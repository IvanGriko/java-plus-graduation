package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventPublicApi;
import ru.practicum.dto.event.*;
import ru.practicum.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPublicController implements EventPublicApi {

    private final EventPublicService eventPublicService;

    @Override
    public List<EventShortDto> getAllEventsByParams(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort eventSort,
            Integer from,
            Integer size,
            HttpServletRequest request
    ) {
        log.info("Получение всех публичных событий");
        EventParams params = EventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .eventSort(eventSort)
                .from(from)
                .size(size)
                .build();
        return eventPublicService.getAllEventsByParams(params, request);
    }

    @Override
    public EventFullDto getInformationAboutEventByEventId(Long userId, Long eventId, HttpServletRequest request) {
        log.info("Получение информации о событии с ID {}", eventId);
        return eventPublicService.getEventById(userId, eventId, request);
    }

    @Override
    public EventCommentDto getEventCommentDto(Long id) {
        log.info("Получение комментария события с ID {}", id);
        return eventPublicService.getEventCommentDto(id);
    }

    @Override
    public Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> ids) {
        log.info("Получение списка комментариев событий");
        return eventPublicService.getEventCommentDtoList(ids);
    }

    @Override
    public EventInteractionDto getEventInteractionDto(Long id) {
        log.info("Получение статистики взаимодействия с событием с ID {}", id);
        return eventPublicService.getEventInteractionDto(id);
    }

    @Override
    public Collection<EventShortDto> getRecommendations(Long userId, Integer size) {
        log.info("Получение рекомендаций для пользователя с ID {}", userId);
        return eventPublicService.getRecommendations(userId, size);
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        log.info("Отправка информации о лайке пользователя с ID {} к событию с ID {}", userId, eventId);
        return eventPublicService.sendLike(userId, eventId);
    }
}