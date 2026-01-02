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

    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Начало обработки сходства событий: {}", eventSimilarityAvro);
        // Определяем минимальное и максимальное ID событий
        Long first = Math.min(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        Long second = Math.max(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        // Проверяем наличие существующего объекта сходства событий
        EventSimilarity existingSimilarity = eventSimilarityRepository.findByEventAAndEventB(first, second);
        if (existingSimilarity == null) {
            // Если запись отсутствует, создаем новую сущность
            EventSimilarity newSimilarity = EventSimilarityMapper.fromAvroToNewEntity(eventSimilarityAvro);
            eventSimilarityRepository.save(newSimilarity);
            log.info("Создана новая запись сходства событий: {}", newSimilarity);
        } else {
            // Если запись существует, обновляем её
            existingSimilarity.setScore(eventSimilarityAvro.getScore());
            existingSimilarity.setTimestamp(eventSimilarityAvro.getTimestamp());
            eventSimilarityRepository.save(existingSimilarity);
            log.info("Обновлена существующая запись сходства событий: {}", existingSimilarity);
        }
    }
}