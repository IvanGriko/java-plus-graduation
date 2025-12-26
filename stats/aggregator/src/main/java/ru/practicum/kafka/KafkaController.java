package ru.practicum.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.kafka.AggregatorProperties;
import ru.practicum.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

//import javax.annotation.PostConstruct;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class KafkaController {

    @Autowired
    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry kafkaRegistry;

    @Value("${my-area-guide.kafka.user-action-topic}")
    String userActionTopic;

    @Autowired
    AggregatorProperties properties;

    @Autowired
    UserActionService userActionService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeKafkaProducer() {
        kafkaTemplate.flush();
        kafkaRegistry.start();
        log.info("Kafka producer успешно инициализирован.");
    }

    @KafkaListener(topics = "${my-area-guide.kafka.user-action-topic}")
    public void processUserAction(UserActionAvro userActionAvro) {
        try {
            userActionService.processUserActivity(userActionAvro);
            log.info("Обработано действие пользователя: {}", userActionAvro);
        } catch (Exception ex) {
            log.error("Ошибка обработки действия пользователя: {}", ex.getMessage(), ex);
        }
    }
}

//@Service
//@RequiredArgsConstructor(onConstructor_ = {@Autowired})
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class KafkaController {
//
//    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
//    KafkaListenerEndpointRegistry kafkaRegistry;
//    AggregatorProperties properties;
//    UserActionService userActionService;
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void initializeKafkaProducer() {
//        kafkaTemplate.flush();
//        kafkaRegistry.start();
//        log.info("Kafka producer успешно инициализирован.");
//    }
//
//    @KafkaListener(topics = "#{properties.kafka.userActionTopic}")
//    public void processUserAction(UserActionAvro userActionAvro) {
//        try {
//            userActionService.processUserActivity(userActionAvro);
//            log.info("Обработано действие пользователя: {}", userActionAvro);
//        } catch (Exception ex) {
//            log.error("Ошибка обработки действия пользователя: {}", ex.getMessage(), ex);
//        }
//    }
//}

//package ru.practicum.kafka;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.avro.specific.SpecificRecordBase;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//import ru.practicum.ewm.stats.avro.UserActionAvro;
//import ru.practicum.properties.AggregatorProperties;
//import ru.practicum.service.UserActionService;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class KafkaController {
//
//    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
//    KafkaListenerEndpointRegistry kafkaRegistry;
//    AggregatorProperties properties;
//    UserActionService userActionService;
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void initKafkaProducer() {
//        kafkaTemplate.flush();
//        kafkaRegistry.start();
//        log.info("Инициализирован Kafka Producer");
//    }
//
//    @KafkaListener(topics = "#{customProperties.kafka.userActionTopic}")
//    public void listen(UserActionAvro userActionAvro) {
//        try {
//            userActionService.handleUserAction(userActionAvro);
//            log.info("Получено и обработано действие пользователя: {}", userActionAvro.toString());
//        } catch (Exception e) {
//            log.error("Ошибка при обработке действия пользователя: {}", e.getMessage(), e);
//        }
//    }
//}