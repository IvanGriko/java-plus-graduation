package ru.practicum.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;
import ru.practicum.service.UserActionService;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaController {

    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    KafkaListenerEndpointRegistry kafkaRegistry;
    CustomProperties customProperties;
    UserActionService userActionService;

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaProducer() {
        kafkaTemplate.flush();
        kafkaRegistry.start();
        log.info("Инициализирован Kafka Producer");
    }

    @KafkaListener(topics = "#{customProperties.kafka.userActionTopic}")
    public void listen(UserActionAvro userActionAvro) {
        try {
            userActionService.handleUserAction(userActionAvro);
            log.info("Получено и обработано действие пользователя: {}", userActionAvro.toString());
        } catch (Exception e) {
            log.error("Ошибка при обработке действия пользователя: {}", e.getMessage(), e);
        }
    }
}