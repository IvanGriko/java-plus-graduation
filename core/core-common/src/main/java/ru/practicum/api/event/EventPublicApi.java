package ru.practicum.api.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventPublicApi {

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    List<EventShortDto> getAllEventsByParams(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventSort eventSort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    );

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    EventFullDto getInformationAboutEventByEventId(
            @RequestHeader("X-EWM-USER-ID")
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId,
            HttpServletRequest request
    );

    @GetMapping("/events/{id}/dto/comment")
    @ResponseStatus(HttpStatus.OK)
    EventCommentDto getEventCommentDto(
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long id
    );

    @PostMapping("/events/dto/list/comment")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventCommentDto> getEventCommentDtoList(
            @RequestBody Collection<Long> ids
    );

    @GetMapping("/events/{id}/dto/interaction")
    @ResponseStatus(HttpStatus.OK)
    EventInteractionDto getEventInteractionDto(
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long id
    );

    @GetMapping("/events/recommendations")
    @ResponseStatus(HttpStatus.OK)
    Collection<EventShortDto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID")
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @RequestParam(defaultValue = "10") Integer size
    );

    @PutMapping("/events/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    String sendLike(
            @RequestHeader("X-EWM-USER-ID")
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId
    );
}