package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.AnalyzerProperties;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService {

    AnalyzerProperties properties;
    UserActionRepository userActionRepository;

    @Transactional
    public void processUserAction(UserActionAvro userActionAvro) {
        log.debug("Обработка входящего действия: {}", userActionAvro);
        BigDecimal calculatedWeight = properties.getAnalyzer().getWeights().convertUserActionToWeight(userActionAvro);
        UserAction processedAction = UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .weight(calculatedWeight)
                .timestamp(userActionAvro.getTimestamp())
                .build();
        userActionRepository.save(processedAction);
        log.debug("Запись о действии успешно создана: {}", processedAction);
    }
}

//@Slf4j
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class UserActionService {
//
//    AnalyzerProperties properties;
//    UserActionRepository userActionRepository;
//
//    @Transactional
//    public void handleUserAction(UserActionAvro userActionAvro) {
//        log.debug("Входящие данные: {}", userActionAvro);
//
//        BigDecimal weight = properties.getAnalyzer().getWeights().convertUserActionToWeight(userActionAvro);
//
//        UserAction userAction = UserAction.builder()
//                .userId(userActionAvro.getUserId())
//                .eventId(userActionAvro.getEventId())
//                .weight(weight)
//                .timestamp(userActionAvro.getTimestamp())
//                .build();
//
//        userActionRepository.save(userAction);
//        log.debug("Создана запись о действии: {}", userAction);
//    }
//}