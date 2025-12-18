package ru.practicum.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.event.EventAllApi;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.event.EventInteractionDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceInteractionException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class EventClientAbstractHelper {

    protected final EventAllApi eventApiClient;

    public EventInteractionDto fetchEventInteractionByIdOrFail(Long eventId) {
        try {
            return eventApiClient.getEventInteractionDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Мероприятие с ID " + eventId + " не найдено.");
            log.warn("Ошибка взаимодействия с сервисом: получено исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new ServiceInteractionException("Не удалось получить информацию о событии с ID " + eventId,
                    "Сервис событий недоступен");
        }
    }

    public EventInteractionDto fetchEventInteractionById(Long eventId) {
        try {
            return eventApiClient.getEventInteractionDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Мероприятие с ID " + eventId + " не найдено.");
            log.warn("Ошибка взаимодействия с сервисом: получено исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return EventInteractionDto.withOnlyId(eventId);
        }
    }

    public EventCommentDto fetchEventCommentByIdOrFail(Long eventId) {
        try {
            return eventApiClient.getEventCommentDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Мероприятие с ID " + eventId + " не найдено.");
            log.warn("Ошибка взаимодействия с сервисом: получено исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new ServiceInteractionException("Не удалось получить информацию о событии с ID " + eventId,
                    "Сервис событий недоступен");
        }
    }

    public EventCommentDto fetchEventCommentById(Long eventId) {
        try {
            return eventApiClient.getEventCommentDto(eventId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Мероприятие с ID " + eventId + " не найдено.");
            log.warn("Ошибка взаимодействия с сервисом: получено исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return EventCommentDto.withOnlyId(eventId);
        }
    }

    public Map<Long, EventCommentDto> fetchEventCommentMapByIds(Collection<Long> eventIdList) {
        try {
            return eventApiClient.getEventCommentDtoList(eventIdList).stream()
                    .collect(Collectors.toMap(EventCommentDto::getId, dto -> dto));
        } catch (RuntimeException e) {
            log.warn("Ошибка взаимодействия с сервисом: получено исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return eventIdList.stream()
                    .collect(Collectors.toMap(id -> id, EventCommentDto::withOnlyId));
        }
    }

    private boolean isNotFoundCode(RuntimeException e) {
        if (e instanceof FeignException.NotFound) return true;
        if (e.getCause() != null && e.getCause() instanceof FeignException.NotFound) return true;
        return false;
    }

}
