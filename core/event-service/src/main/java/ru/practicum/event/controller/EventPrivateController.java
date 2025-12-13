package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventPrivateApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;
import ru.practicum.event.service.EventPrivateService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController implements EventPrivateApi {

    private final EventPrivateService eventPrivateService;

    @Override
    public EventFullDto addNewEventByUser(Long userId, NewEventDto newEventDto) {
        log.info("Пользователь с ID {} добавил новое событие", userId);
        return eventPrivateService.addEvent(userId, newEventDto);
    }

    @Override
    public Collection<EventShortDto> getAllEventsByUserId(Long userId, Integer from, Integer size) {
        log.info("Получение всех событий пользователя с ID {}", userId);
        return eventPrivateService.getEventsByUserId(userId, from, size);
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        log.info("Получение события с ID {} пользователем с ID {}", eventId, userId);
        return eventPrivateService.getEventByUserIdAndEventId(userId, eventId);
    }

    @Override
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        log.info("Пользователь с ID {} обновляет событие с ID {}", userId, eventId);
        return eventPrivateService.updateEventByUserIdAndEventId(userId, eventId, updateEventDto);
    }

}