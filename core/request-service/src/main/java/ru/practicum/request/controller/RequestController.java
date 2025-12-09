package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.request.RequestApi;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController implements RequestApi {

    private final RequestService requestService;

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Пользователь с ID {} подал заявку на участие в событии с ID {}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Пользователь с ID {} отменяет заявку с ID {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @Override
    public Collection<ParticipationRequestDto> getRequesterRequests(Long userId) {
        log.info("Получение заявок пользователя с ID {}", userId);
        return requestService.findRequesterRequests(userId);
    }

    @Override
    public EventRequestStatusUpdateResultDto moderateRequest(Long userId, Long eventId,
                                                             EventRequestStatusUpdateRequestDto updateRequestDto) {
        log.info("Модерация заявок на событие с ID {} пользователем с ID {}", eventId, userId);
        return requestService.moderateRequest(userId, eventId, updateRequestDto);
    }

    @Override
    public Collection<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие с ID {} пользователем с ID {}", eventId, userId);
        return requestService.findEventRequests(userId, eventId);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        log.info("Получение подтвержденных заявок для событий с ID {}", eventIds);
        return requestService.getConfirmedRequestsByEventIds(eventIds);
    }

}