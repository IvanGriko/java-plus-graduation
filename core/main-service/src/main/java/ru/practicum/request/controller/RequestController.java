package ru.practicum.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(
            @PathVariable @Positive(message = "Указан некорректный идентификатор пользователя") Long userId,
            @RequestParam @Positive(message = "Указан некорректный идентификатор события") Long eventId
    ) {
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive(message = "Указан некорректный идентификатор пользователя") Long userId,
            @PathVariable @Positive(message = "Указан некорректный идентификатор запроса") Long requestId
    ) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/requests")
    public Collection<ParticipationRequestDto> getRequesterRequests(
            @PathVariable @Positive(message = "Указан некорректный идентификатор пользователя") Long userId
    ) {
        return requestService.findRequesterRequests(userId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto moderateRequest(
            @PathVariable @Positive(message = "Указан некорректный идентификатор пользователя") Long userId,
            @PathVariable @Positive(message = "Указан некорректный идентификатор события") Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        return requestService.moderateRequest(userId, eventId, updateRequestDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive(message = "Указан некорректный идентификатор пользователя") Long userId,
            @PathVariable @Positive(message = "Указан некорректный идентификатор события") Long eventId
    ) {
        return requestService.findEventRequests(userId, eventId);
    }
}
