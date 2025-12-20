package ru.practicum.api.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventDto;

import java.util.Collection;

public interface EventPrivateApi {

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addNewEventByUser(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @Valid
            @RequestBody NewEventDto newEventDto
    );

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventShortDto> getAllEventsByUserId(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto getEventByUserIdAndEventId(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId
    );

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto updateEventByUserIdAndEventId(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId,
            @Valid
            @RequestBody UpdateEventDto updateEventDto
    );
}