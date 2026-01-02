package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.properties.CustomProperties;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionService {

    CustomProperties customProperties;
    UserActionRepository userActionRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserAction(UserActionAvro userActionAvro) {
        log.info("Получено новое действие пользователя: {}", userActionAvro);
        // Определение веса действия
        BigDecimal weight = customProperties.getAnalyzer().getWeights().ofUserAction(userActionAvro);
        // Создание сущности действия пользователя
        UserAction userAction = UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .weight(weight)
                .timestamp(userActionAvro.getTimestamp())
                .build();
        // Сохранение действия в репозиторий
        userActionRepository.save(userAction);
        log.info("Действие успешно сохранено: {}", userAction);
    }
}