package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.service.EventPrivateService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addNewEventByUser(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        log.info("Добавление нового события пользователем с идентификатором {}", userId);
        return eventPrivateService.addEvent(userId, newEventDto);
    }

    @GetMapping
    Collection<EventShortDto> getAllEventsByUserId(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") Long from,
            @RequestParam(defaultValue = "10") Long size
    ) {
        log.info("Получение всех событий пользователя с идентификатором {}", userId);
        return eventPrivateService.getEventsByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    EventFullDto getEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    ) {
        log.info("Получение события с идентификатором {} пользователя с идентификатором {}", eventId, userId);
        return eventPrivateService.getEventByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    EventFullDto updateEventByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventDto updateEventDto
    ) {
        log.info("Обновление события с идентификатором {} пользователя с идентификатором {}. Данные обновления: {}",
                eventId, userId, updateEventDto);
        return eventPrivateService.updateEventByUserIdAndEventId(userId, eventId, updateEventDto);
    }
}