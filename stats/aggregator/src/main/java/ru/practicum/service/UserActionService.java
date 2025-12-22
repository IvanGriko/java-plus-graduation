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
import ru.practicum.properties.AggregatorProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService {

    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    AggregatorProperties properties;

    // Карта связей пользователей и событий с соответствующими весами
    ConcurrentHashMap<Long, Map<Long, BigDecimal>> userWeights = new ConcurrentHashMap<>();
    // Суммарные веса событий
    ConcurrentHashMap<Long, BigDecimal> eventTotalWeights = new ConcurrentHashMap<>();
    // Матрица наименьших весов для сравнения похожих событий
    ConcurrentHashMap<Long, Map<Long, BigDecimal>> minimalWeightMatrix = new ConcurrentHashMap<>();

    public void processUserActivity(UserActionAvro action) {
        Long userId = action.getUserId();
        Long eventId = action.getEventId();
        BigDecimal newWeight = properties.getAggregator().getWeights().calculateWeight(action);
        BigDecimal oldWeight = findOldWeight(userId, eventId);

        if (oldWeight.compareTo(newWeight) >= 0) {
            log.debug("Нет изменений в весе, пропускаем обновление.");
            return;
        }

        log.debug("Обновляем вес события {} для пользователя {} до {}.", eventId, userId, newWeight);

        updateWeights(userId, eventId, newWeight);
        recalculateEventTotals(eventId, oldWeight, newWeight);
        adjustMinWeightMatrix(userId, eventId, oldWeight, newWeight);
        broadcastSimilarities(userId, eventId);
    }

    private BigDecimal findOldWeight(Long userId, Long eventId) {
        return userWeights.getOrDefault(userId, Collections.emptyMap())
                .getOrDefault(eventId, BigDecimal.ZERO);
    }

    private void updateWeights(Long userId, Long eventId, BigDecimal newWeight) {
        userWeights.computeIfAbsent(userId, u -> new HashMap<>())
                .put(eventId, newWeight);
    }

    private void recalculateEventTotals(Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        BigDecimal adjustmentDelta = newWeight.subtract(oldWeight);
        eventTotalWeights.merge(eventId, adjustmentDelta, BigDecimal::add);
        log.debug("Суммарный вес события {} обновлён до {}.", eventId, eventTotalWeights.get(eventId));
    }

    private void adjustMinWeightMatrix(Long userId, Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        if (properties.getAggregator().getMinimumSumAlgorithm().equalsIgnoreCase("naive")) {
            naiveRecalculation(userId, eventId);
        } else {
            optimizedRecalculation(userId, eventId, oldWeight, newWeight);
        }
    }

    private void naiveRecalculation(Long userId, Long eventId) {
        for (Long secondEventId : userWeights.get(userId).keySet()) {
            if (!secondEventId.equals(eventId)) {
                recalculateMinWeightForPair(eventId, secondEventId);
            }
        }
    }

    private void optimizedRecalculation(Long userId, Long eventId, BigDecimal oldWeight, BigDecimal newWeight) {
        for (Map.Entry<Long, BigDecimal> otherEventEntry : userWeights.get(userId).entrySet()) {
            Long otherEventId = otherEventEntry.getKey();
            if (!otherEventId.equals(eventId)) {
                recalculateMinWeightForPairOptimized(eventId, otherEventId, oldWeight, newWeight);
            }
        }
    }

    private void recalculateMinWeightForPair(Long eventId, Long otherEventId) {
        Set<Long> intersectedUsers = intersectUserSets(eventId, otherEventId);
        BigDecimal totalMinWeight = intersectedUsers.parallelStream()
                .map(uid -> getUserWeight(eventId, uid).min(getUserWeight(otherEventId, uid)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        storeMinWeight(eventId, otherEventId, totalMinWeight);
    }

    private void recalculateMinWeightForPairOptimized(Long eventId, Long otherEventId, BigDecimal oldWeight, BigDecimal newWeight) {
        BigDecimal oldMinWeight = oldWeight.min(getUserWeight(otherEventId, eventId));
        BigDecimal newMinWeight = newWeight.min(getUserWeight(otherEventId, eventId));
        BigDecimal adjustedMinWeight = getMinWeight(eventId, otherEventId).subtract(oldMinWeight).add(newMinWeight);
        storeMinWeight(eventId, otherEventId, adjustedMinWeight);
    }

    private Set<Long> intersectUserSets(Long eventId1, Long eventId2) {
        Set<Long> set1 = userWeights.entrySet().parallelStream()
                .filter(entry -> entry.getValue().containsKey(eventId1))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Long> set2 = userWeights.entrySet().parallelStream()
                .filter(entry -> entry.getValue().containsKey(eventId2))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        set1.retainAll(set2);
        return set1;
    }

    private BigDecimal getUserWeight(Long eventId, Long userId) {
        return userWeights.get(userId).getOrDefault(eventId, BigDecimal.ZERO);
    }

    private BigDecimal getMinWeight(Long eventId1, Long eventId2) {
        return minimalWeightMatrix.computeIfAbsent(eventId1, k -> new HashMap<>())
                .getOrDefault(eventId2, BigDecimal.ZERO);
    }

    private void storeMinWeight(Long eventId1, Long eventId2, BigDecimal weight) {
        minimalWeightMatrix.computeIfAbsent(eventId1, k -> new HashMap<>())
                .put(eventId2, weight);
    }

    private void broadcastSimilarities(Long userId, Long eventId) {
        userWeights.get(userId).keySet().stream()
                .filter(otherEventId -> !otherEventId.equals(eventId))
                .forEach(otherEventId -> calculateAndSendSimilarity(eventId, otherEventId));
    }

    private void calculateAndSendSimilarity(Long eventId1, Long eventId2) {
        BigDecimal numerator = getMinWeight(eventId1, eventId2);
        BigDecimal denominator = multiplySquareRoots(eventId1, eventId2);
        double similarity = numerator.doubleValue() / denominator.doubleValue();
        EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                .setEventA(eventId1)
                .setEventB(eventId2)
                .setScore(similarity)
                .setTimestamp(Instant.now())
                .build();
        kafkaTemplate.send(properties.getKafka().getEventsSimilarityTopic(), similarityAvro);
        log.debug("Отправлена мера сходства для событий {} и {}: {}", eventId1, eventId2, similarity);
    }

    private BigDecimal multiplySquareRoots(Long eventId1, Long eventId2) {
        return BigDecimal.valueOf(Math.sqrt(eventTotalWeights.get(eventId1).doubleValue()))
                .multiply(BigDecimal.valueOf(Math.sqrt(eventTotalWeights.get(eventId2).doubleValue())));
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
//import ru.practicum.properties.AggregatorProperties;
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
//    AggregatorProperties properties;
//
//    Map<Long, Map<Long, BigDecimal>> weightsMapByUserId = new HashMap<>();
//    Map<Long, Map<Long, BigDecimal>> weightsMapByEventId = new HashMap<>();
//    Map<Long, BigDecimal> eventSums = new HashMap<>();
//    Map<Long, Map<Long, BigDecimal>> minWeightSums = new HashMap<>();
//
//    public void handleUserAction(UserActionAvro userActionAvro) {
//        Long userId = userActionAvro.getUserId();
//        Long eventId = userActionAvro.getEventId();
//        BigDecimal oldWeight = BigDecimal.ZERO;
//        BigDecimal newWeight = properties.getAggregator().getWeights().ofUserAction(userActionAvro);
//        log.debug("Входящие данные: новый вес {} события {} для пользователя {}",
//                newWeight, eventId, userId);
//
//        Map<Long, BigDecimal> userWeights = weightsMapByUserId.computeIfAbsent(userId, id -> new HashMap<>());
//        Map<Long, BigDecimal> eventWeights = weightsMapByEventId.computeIfAbsent(eventId, id -> new HashMap<>());
//
//        if (userWeights.containsKey(eventId)) {
//            oldWeight = userWeights.get(eventId);
//            if (newWeight.compareTo(oldWeight) <= 0) {
//                log.debug("Вес не меняется, так как новый вес меньше или равен старому");
//                return;
//            }
//            log.debug("Вес обновлён до {}", newWeight);
//        } else {
//            log.debug("Установлен вес {}, так как пользователь {} впервые выполняет действие с событием {}",
//                    newWeight, userId, eventId);
//        }
//        userWeights.put(eventId, newWeight);
//        eventWeights.put(userId, newWeight);
//
//        recountEventSum(eventId, oldWeight, newWeight);
//        if (properties.getAggregator().getMinimumSumAlgorithm().toLowerCase().contains("naive")) {
//            recountEventMinWeightsNaive(userId, eventId);
//        } else {
//            recountEventMinWeightsOptimized(userId, eventId, oldWeight, newWeight);
//        }
//        sendSimilarity(userId, eventId);
//    }
//
//    private void sendSimilarity(Long userId, Long eventId) {
//        for (Long anotherEventId : weightsMapByUserId.get(userId).keySet()) {
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
//                kafkaTemplate.send(properties.getKafka().getEventsSimilarityTopic(), eventSimilarityAvro);
//                log.debug("Отправлено сходство: {}", eventSimilarityAvro);
//            }
//        }
//    }
//
//    // пересчет таблицы сумм векторов события
//    private void recountEventSum(
//            Long eventId,
//            BigDecimal oldWeight,
//            BigDecimal newWeight
//    ) {
//        BigDecimal delta = newWeight.subtract(oldWeight);
//        BigDecimal prevSum = eventSums.get(eventId);
//        eventSums.merge(eventId, delta, BigDecimal::add);
//        log.debug("Для события {} посчитана сумма весов {} + {} = {}", eventId, prevSum, delta, eventSums.get(eventId));
//    }
//
//    // пересчет таблицы сумм минимумов двух векторов событий - версия 1, наивная
//    private void recountEventMinWeightsNaive(Long userId, Long eventId) {
//        for (Long secondEventId : weightsMapByEventId.get(userId).keySet()) {
//            if (!Objects.equals(secondEventId, eventId)) {
//                Map<Long, BigDecimal> eventWeights1 = weightsMapByEventId.get(eventId);
//                Map<Long, BigDecimal> eventWeights2 = weightsMapByEventId.get(secondEventId);
//                Set<Long> userIds = new HashSet<>(eventWeights1.keySet());
//                userIds.retainAll(eventWeights2.keySet());
//                BigDecimal sum = userIds.stream()
//                        .map(id -> eventWeights1.get(id).min(eventWeights2.get(id)))
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//                Long first = Math.min(eventId, secondEventId);
//                Long second = Math.max(eventId, secondEventId);
//                minWeightSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, sum);;
//                log.debug("Для событий {} и {} посчитана сумма минимумов весов {}", first, second, sum);
//            }
//        }
//    }
//
//    // пересчет таблицы сумм минимумов двух векторов событий - версия 2, оптимизированная
//    private void recountEventMinWeightsOptimized(
//            Long userId,
//            Long eventId,
//            BigDecimal oldWeight,
//            BigDecimal newWeight
//    ) {
//        for (Map.Entry<Long, BigDecimal> anotherEventEntry : weightsMapByUserId.get(userId).entrySet()) {
//            if (!Objects.equals(eventId, anotherEventEntry.getKey())) {
//                Long first = Math.min(eventId, anotherEventEntry.getKey());
//                Long second = Math.max(eventId, anotherEventEntry.getKey());
//                Map<Long, BigDecimal> firstEventSums = minWeightSums.computeIfAbsent(first, k -> new HashMap<>());
//                BigDecimal oldSum = firstEventSums.getOrDefault(second, BigDecimal.ZERO);
//                BigDecimal oldMinimum = oldWeight.min(anotherEventEntry.getValue());
//                BigDecimal newMinimum = newWeight.min(anotherEventEntry.getValue());
//                BigDecimal newSum = oldSum.subtract(oldMinimum).add(newMinimum);
//                firstEventSums.put(second, newSum);
//                log.debug("Для событий {} и {} посчитана сумма минимумов {}", first, second, newSum);
//            }
//        }
//    }
//}