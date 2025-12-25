package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducerInitializer {

    private final KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initKafkaProducer() {
        kafkaTemplate.flush();
    }

}


//package ru.practicum.kafka;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.avro.specific.SpecificRecordBase;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaProducerInitializer {
//
//    private final KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void initKafkaProducer() {
//        try {
//            log.info("Инициализация Kafka-производителя...");
//            kafkaTemplate.flush();
//            log.info("Kafka-производитель успешно инициализирован!");
//        } catch (Exception e) {
//            log.error("Ошибка при инициализации Kafka-производителя:", e);
//            throw new RuntimeException("Ошибка инициализации Kafka-производителя", e);
//        }
//    }
//}