package ru.practicum.service;

import com.google.protobuf.TextFormat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.user.action.UserActionProto;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.properties.CollectorProperties;

//@Slf4j
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class UserActionService {
//
//    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
//    CollectorProperties properties;
//
//    public void handleUserAction(UserActionProto userActionProto) {
//        log.info("Получено Proto: {}", TextFormat.printer().emittingSingleLine(true).printToString(userActionProto));
//        if (userActionProto.getUserId() <= 0 || userActionProto.getEventId() <= 0) {
//            log.warn("Некорректные данные: {}", userActionProto);
//            return;
//        }
//        UserActionAvro userActionAvro = UserActionMapper.fromProtoToAvro(userActionProto);
//        try {
//            kafkaTemplate.send(properties.getKafka().getUserActionTopic(), userActionAvro);
//            log.info("Отправлено Avro: {}", userActionAvro);
//        } catch (Exception e) {
//            log.error("Ошибка при отправке в Kafka: {}", e.getMessage(), e);
//            throw new RuntimeException("Ошибка отправки в Kafka", e);
//        }
//    }
//}

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService {

    KafkaTemplate<Void, SpecificRecordBase> kafkaTemplate;
    CollectorProperties properties;

    public void handleUserAction(UserActionProto userActionProto) {
        log.info("Получено Proto: {}", TextFormat.printer().emittingSingleLine(true).printToString(userActionProto));
        UserActionAvro userActionAvro = UserActionMapper.fromProtoToAvro(userActionProto);
        kafkaTemplate.send(properties.getKafka().getUserActionTopic(), userActionAvro);
        log.info("Отправлено Avro: {}", userActionAvro);
    }
}