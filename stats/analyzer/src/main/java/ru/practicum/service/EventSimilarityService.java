package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.EventSimilarityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityService {

    private final EventSimilarityRepository repository;

    @Transactional
    public void processEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.debug("Получены данные: {}", eventSimilarityAvro);
        Long firstEventId = Math.min(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        Long secondEventId = Math.max(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        EventSimilarity entity = repository.findByEventAAndEventB(firstEventId, secondEventId);
        if (entity == null) {
            entity = EventSimilarityMapper.fromAvroToSimilarity(eventSimilarityAvro);
            log.info("Запись сходства создана для событий {} и {}", firstEventId, secondEventId);
        } else {
            entity.updateFields(eventSimilarityAvro.getScore(), eventSimilarityAvro.getTimestamp());
            log.info("Запись сходства обновлена для событий {} и {}", firstEventId, secondEventId);
        }
        repository.save(entity);
        log.debug("Итоговая запись сходства: {}", entity);
    }
}

//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EventSimilarityService {
//
//    private final EventSimilarityRepository eventSimilarityRepository;
//
//    @Transactional
//    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
//        log.debug("Входящие данные: {}", eventSimilarityAvro);
//        String logAction;
//        Long first = Math.min(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
//        Long second = Math.max(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
//        EventSimilarity eventSimilarity = eventSimilarityRepository.findByEventAAndEventB(first, second);
//        if (eventSimilarity == null) {
//            eventSimilarity = EventSimilarityMapper.fromAvroToNewEntity(eventSimilarityAvro);
//            logAction = "Создана";
//        } else {
//            eventSimilarity.setScore(eventSimilarityAvro.getScore());
//            eventSimilarity.setTimestamp(eventSimilarityAvro.getTimestamp());
//            logAction = "Обновлена";
//        }
//        eventSimilarityRepository.save(eventSimilarity);
//        log.debug("{} запись сходства событий {} и {}. Установлено значение {}",
//                logAction, eventSimilarity.getEventA(), eventSimilarity.getEventB(), eventSimilarity.getScore());
//    }
//}