package ru.practicum.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;
import ru.practicum.service.EventSimilarityService;
import ru.practicum.service.UserActionService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaController {

    KafkaListenerEndpointRegistry kafkaRegistry;
    CustomProperties customProperties;
    UserActionService userActionService;
    EventSimilarityService eventSimilarityService;

    @KafkaListener(
            topics = "#{customProperties.kafka.userActionTopic}",
            containerFactory = "userActionListenerContainerFactory"
    )
    public void listenUserAction(UserActionAvro userActionAvro) {
        userActionService.handleUserAction(userActionAvro);
    }

    @KafkaListener(
            topics = "#{customProperties.kafka.eventsSimilarityTopic}",
            containerFactory = "eventsSimilarityListenerContainerFactory"
    )
    public void listenEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        eventSimilarityService.handleEventSimilarity(eventSimilarityAvro);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaProducer() {
        kafkaRegistry.start();
    }
}