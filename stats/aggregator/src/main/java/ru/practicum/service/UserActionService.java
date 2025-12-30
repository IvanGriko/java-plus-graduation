package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService {

    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    CustomProperties customProperties;

    // Веса взаимодействия по юзеру
    Map<Long, Map<Long, BigDecimal>> weightsByUser = new HashMap<>();

    // Веса взаимодействия по событию
    Map<Long, Map<Long, BigDecimal>> weightsByEvent = new HashMap<>();

    // Суммарные веса событий
    Map<Long, BigDecimal> eventSums = new HashMap<>();

    // Минимальные веса между парами событий
    Map<Long, Map<Long, BigDecimal>> minWeightSums = new HashMap<>();

    public void processUserAction(UserActionAvro userActionAvro) {
        Long userId = userActionAvro.getUserId();
        Long eventId = userActionAvro.getEventId();
        BigDecimal oldWeight = BigDecimal.ZERO;
        BigDecimal newWeight = customProperties.getAggregator().getWeights().ofUserAction(userActionAvro);
        log.info("Обработка нового взаимодействия пользователя {} с событием {}, новый вес {}", userId, eventId, newWeight);

        // Получение карт весов для текущего пользователя и события
        Map<Long, BigDecimal> userWeights = weightsByUser.computeIfAbsent(userId, id -> new HashMap<>());
        Map<Long, BigDecimal> eventWeights = weightsByEvent.computeIfAbsent(eventId, id -> new HashMap<>());

        // Проверка наличия ранее зарегистрированного веса
        if (userWeights.containsKey(eventId)) {
            oldWeight = userWeights.get(eventId);
            if (newWeight.compareTo(oldWeight) <= 0) { // Если новый вес меньше старого, игнорируем изменение
                log.info("Вес {} меньше или равен предыдущему значению {}, обновление не требуется.", newWeight, oldWeight);
                return;
            }
            log.info("Новое значение веса больше предыдущего: {}, заменяем на {}.", oldWeight, newWeight);
        } else {
            log.info("Первое взаимодействие пользователя {} и события {}, обновляем вес до {}.", userId, eventId, newWeight);
        }

        // Обновление весов
        userWeights.put(eventId, newWeight);
        eventWeights.put(userId, newWeight);

        // Пересчёт суммарного веса события
        recountEventSum(eventId, oldWeight, newWeight);

        // Выбор алгоритма пересчёта минимума весов
        if ("naive".equalsIgnoreCase(customProperties.getAggregator().getMinimumSumAlgorithm())) {
            recountEventMinWeightsNaive(userId, eventId);
        } else {
            recountEventMinWeightsOptimized(userId, eventId, oldWeight, newWeight);
        }

        // Отправка похожих событий
        sendSimilarity(userId, eventId);
    }

    private void sendSimilarity(Long userId, Long eventId) {
        for (Long anotherEventId : weightsByUser.get(userId).keySet()) {
            if (!Objects.equals(eventId, anotherEventId)) {
                long first = Math.min(eventId, anotherEventId);
                long second = Math.max(eventId, anotherEventId);
                double numerator = minWeightSums.get(first).get(second).doubleValue();
                double sqrt1 = Math.sqrt(eventSums.get(first).doubleValue());
                double sqrt2 = Math.sqrt(eventSums.get(second).doubleValue());
                double denominator = sqrt1 * sqrt2;
                double similarity = numerator / denominator;
                EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(similarity)
                        .setTimestamp(Instant.now())
                        .build();
                kafkaTemplate.send(customProperties.getKafka().getEventsSimilarityTopic(), eventSimilarityAvro);
                log.info("Сообщение о схожести событий отправлено: {}", eventSimilarityAvro);
            }
        }
    }

    private void recountEventSum(Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        BigDecimal delta = newWeight.subtract(oldWeight);
        BigDecimal prevSum = eventSums.get(eventId);
        eventSums.merge(eventId, delta, BigDecimal::add);
        log.info("Сумма весов для события {} пересчитана: {} + {} = {}", eventId, prevSum, delta, eventSums.get(eventId));
    }

    private void recountEventMinWeightsNaive(Long userId, Long eventId) {
        for (Long secondEventId : weightsByUser.get(userId).keySet()) {
            if (!Objects.equals(secondEventId, eventId)) {
                Map<Long, BigDecimal> eventWeights1 = weightsByEvent.get(eventId);
                Map<Long, BigDecimal> eventWeights2 = weightsByEvent.get(secondEventId);
                Set<Long> commonUsers = new HashSet<>(eventWeights1.keySet());
                commonUsers.retainAll(eventWeights2.keySet());
                BigDecimal sum = commonUsers.stream()
                        .map(id -> eventWeights1.get(id).min(eventWeights2.get(id)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                Long first = Math.min(eventId, secondEventId);
                Long second = Math.max(eventId, secondEventId);
                minWeightSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, sum);
                log.info("Минимальные веса для событий {} и {} пересчитаны: {}", first, second, sum);
            }
        }
    }

    private void recountEventMinWeightsOptimized(Long userId, Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        for (Map.Entry<Long, BigDecimal> anotherEventEntry : weightsByUser.get(userId).entrySet()) {
            if (!Objects.equals(eventId, anotherEventEntry.getKey())) {
                Long first = Math.min(eventId, anotherEventEntry.getKey());
                Long second = Math.max(eventId, anotherEventEntry.getKey());
                Map<Long, BigDecimal> firstEventSums = minWeightSums.computeIfAbsent(first, k -> new HashMap<>());
                BigDecimal oldSum = firstEventSums.getOrDefault(second, BigDecimal.ZERO);
                BigDecimal oldMinimum = oldWeight.min(anotherEventEntry.getValue());
                BigDecimal newMinimum = newWeight.min(anotherEventEntry.getValue());
                BigDecimal newSum = oldSum.subtract(oldMinimum).add(newMinimum);
                firstEventSums.put(second, newSum);
                log.info("Оптимизированный пересчёт минимальных весов для событий {} и {}: {}", first, second, newSum);
            }
        }
    }
}

//package ru.practicum.service;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.avro.specific.SpecificRecordBase;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
//import ru.practicum.ewm.stats.avro.UserActionAvro;
//import ru.practicum.properties.CustomProperties;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class UserActionService {
//
//    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
//    CustomProperties customProperties;
//
//    // Карта весов взаимодействий пользователей с событиями,
//    // позволяет быстро получать веса конкретного пользователя по событию.
//    Map<Long, Map<Long, BigDecimal>> weightsByUser = new HashMap<>();
//
//    // Карта весов взаимодействий пользователей с событиями,
//    // позволяет быстро получать веса конкретного события по пользователям.
//    Map<Long, Map<Long, BigDecimal>> weightsByEvent = new HashMap<>();
//
//    // Карта суммируемых весов для каждого события,
//    // хранит суммарные значения всех весов, присвоенных данному событию пользователями.
//    Map<Long, BigDecimal> eventSums = new HashMap<>();
//
//    // Карта, содержащая минимальные веса между парами событий,
//    // ключом является одно событие, значением — карта второго события и минимальной суммы весов между ними.
//    Map<Long, Map<Long, BigDecimal>> minWeightSums = new HashMap<>();
//
//    public void processUserAction(UserActionAvro userActionAvro) {
//        Long userId = userActionAvro.getUserId();
//        Long eventId = userActionAvro.getEventId();
//        BigDecimal oldWeight = BigDecimal.ZERO;
//        BigDecimal newWeight = customProperties.getAggregator().getWeights().ofUserAction(userActionAvro);
//        log.info("Получено взаимодействие пользователя {} с событием{}, новый вес {}", userId, eventId, newWeight);
//        Map<Long, BigDecimal> userWeights = weightsByUser.computeIfAbsent(userId, id -> new HashMap<>());
//        Map<Long, BigDecimal> eventWeights = weightsByEvent.computeIfAbsent(eventId, id -> new HashMap<>());
//        if (userWeights.containsKey(eventId)) {
//            oldWeight = userWeights.get(eventId);
//            if (newWeight.compareTo(oldWeight) <= 0) {
//                log.info("Вес {} меньше или равен предыдущему значению {}, обновление не требуется.", newWeight, oldWeight);
//                return;
//            }
//            log.info("Новый вес больше предыдущего: {}, заменяем значение на {}.", oldWeight, newWeight);
//        } else {
//            log.info("Первое взаимодействие пользователя {} и события {}, обновляем вес до {}.", userId, eventId, newWeight);
//        }
//        userWeights.put(eventId, newWeight);
//        eventWeights.put(userId, newWeight);
//        recountEventSum(eventId, oldWeight, newWeight);
//        if ("naive".equalsIgnoreCase(customProperties.getAggregator().getMinimumSumAlgorithm())) {
//            recountEventMinWeightsNaive(userId, eventId);
//        } else {
//            recountEventMinWeightsOptimized(userId, eventId, oldWeight, newWeight);
//        }
//        sendSimilarity(userId, eventId);
//    }
//
//    private void sendSimilarity(Long userId, Long eventId) {
//        for (Long anotherEventId : weightsByUser.get(userId).keySet()) {
//            if (!Objects.equals(eventId, anotherEventId)) {
//                long first = Math.min(eventId, anotherEventId);
//                long second = Math.max(eventId, anotherEventId);
//                double numerator = minWeightSums.get(first).get(second).doubleValue();
//                double sqrt1 = Math.sqrt(eventSums.get(first).doubleValue());
//                double sqrt2 = Math.sqrt(eventSums.get(second).doubleValue());
//                double denominator = sqrt1 * sqrt2;
//                double similarity = numerator / denominator;
//                EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
//                        .setEventA(first)
//                        .setEventB(second)
//                        .setScore(similarity)
//                        .setTimestamp(Instant.now())
//                        .build();
//                kafkaTemplate.send(customProperties.getKafka().getEventsSimilarityTopic(), eventSimilarityAvro);
//                log.info("Отправлено сообщение о схожести событий: {}", eventSimilarityAvro);
//            }
//        }
//    }
//
//    private void recountEventSum(Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
//        BigDecimal delta = newWeight.subtract(oldWeight);
//        BigDecimal prevSum = eventSums.get(eventId);
//        eventSums.merge(eventId, delta, BigDecimal::add);
//        log.info("Сумма весов для события {} пересчитана: {} + {} = {}", eventId, prevSum, delta, eventSums.get(eventId));
//    }
//
//    private void recountEventMinWeightsNaive(Long userId, Long eventId) {
//        for (Long secondEventId : weightsByUser.get(userId).keySet()) {
//            if (!Objects.equals(secondEventId, eventId)) {
//                Map<Long, BigDecimal> eventWeights1 = weightsByEvent.get(eventId);
//                Map<Long, BigDecimal> eventWeights2 = weightsByEvent.get(secondEventId);
//                Set<Long> userIds = new HashSet<>(eventWeights1.keySet());
//                userIds.retainAll(eventWeights2.keySet());
//                BigDecimal sum = userIds.stream()
//                        .map(id -> eventWeights1.get(id).min(eventWeights2.get(id)))
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//                Long first = Math.min(eventId, secondEventId);
//                Long second = Math.max(eventId, secondEventId);
//                minWeightSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, sum);
//                log.info("Пересчитаны минимальные веса для событий {} и {}: {}", first, second, sum);
//            }
//        }
//    }
//
//    private void recountEventMinWeightsOptimized(Long userId, Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
//        for (Map.Entry<Long, BigDecimal> anotherEventEntry : weightsByUser.get(userId).entrySet()) {
//            if (!Objects.equals(eventId, anotherEventEntry.getKey())) {
//                Long first = Math.min(eventId, anotherEventEntry.getKey());
//                Long second = Math.max(eventId, anotherEventEntry.getKey());
//                Map<Long, BigDecimal> firstEventSums = minWeightSums.computeIfAbsent(first, k -> new HashMap<>());
//                BigDecimal oldSum = firstEventSums.getOrDefault(second, BigDecimal.ZERO);
//                BigDecimal oldMinimum = oldWeight.min(anotherEventEntry.getValue());
//                BigDecimal newMinimum = newWeight.min(anotherEventEntry.getValue());
//                BigDecimal newSum = oldSum.subtract(oldMinimum).add(newMinimum);
//                firstEventSums.put(second, newSum);
//                log.info("Пересчитаны минимальные веса для событий {} и {}: {}", first, second, newSum);
//            }
//        }
//    }
//}