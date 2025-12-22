package ru.practicum.kafka.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.AnalyzerProperties;
import ru.practicum.service.EventSimilarityService;
import ru.practicum.service.UserActionService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KafkaController {

    KafkaListenerEndpointRegistry kafkaRegistry;
    AnalyzerProperties properties;
    UserActionService userActionService;
    EventSimilarityService eventSimilarityService;

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaConsumer() {
        try {
            log.info("Инициализация Kafka-потребителей...");
            kafkaRegistry.start(); // Начинаем работу потребителей
            log.info("Kafka-потребители успешно инициализированы.");
        } catch (Exception e) {
            log.error("Ошибка при инициализации Kafka-потребителей: {}", e.getMessage(), e);
            throw new IllegalStateException("Ошибка инициализации Kafka-потребителей", e);
        }
    }

    @KafkaListener(
            topics = "#{customProperties.kafka.userActionTopic}",
            containerFactory = "userActionListenerContainerFactory"
    )
    public void listenUserAction(UserActionAvro userActionAvro) {
        userActionService.processUserAction(userActionAvro);
        log.info("Принято и обработано действие пользователя: {}", userActionAvro);
    }

    @KafkaListener(
            topics = "#{customProperties.kafka.eventsSimilarityTopic}",
            containerFactory = "eventsSimilarityListenerContainerFactory"
    )
    public void listenEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        eventSimilarityService.processEventSimilarity(eventSimilarityAvro);
        log.info("Принята и обработана информация о сходстве событий: {}", eventSimilarityAvro);
    }
}