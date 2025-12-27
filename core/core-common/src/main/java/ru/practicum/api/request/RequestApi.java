package ru.practicum.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.Collection;
import java.util.Map;

public interface RequestApi {

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequest(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId,
            @RequestParam
            @Positive(message = "Неверный идентификатор мероприятия") Long eventId
    );

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    ParticipationRequestDto cancelRequest(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId,
            @PathVariable
            @Positive(message = "Неверный идентификатор заявки") Long requestId
    );

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> getRequesterRequests(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId
    );

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    EventRequestStatusUpdateResultDto moderateRequest(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId,
            @PathVariable
            @Positive(message = "Неверный идентификатор мероприятия") Long eventId,
            @RequestBody
            @Valid EventRequestStatusUpdateRequestDto updateRequestDto
    );

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    Collection<ParticipationRequestDto> getEventRequests(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId,
            @PathVariable
            @Positive(message = "Неверный идентификатор мероприятия") Long eventId
    );

    @PostMapping("/requests/confirmed")
    @ResponseStatus(HttpStatus.OK)
    Map<Long, Long> getConfirmedRequestsByEventIds(
            @RequestBody Collection<Long> eventIds
    );

    @GetMapping("/users/{userId}/events/{eventId}/check/participation")
    @ResponseStatus(HttpStatus.OK)
    String checkParticipation(
            @PathVariable
            @Positive(message = "Неверный идентификатор пользователя") Long userId,
            @PathVariable
            @Positive(message = "Неверный идентификатор мероприятия") Long eventId
    );
}