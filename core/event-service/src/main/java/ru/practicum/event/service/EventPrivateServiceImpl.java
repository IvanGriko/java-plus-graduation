package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.category.dal.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPrivateServiceImpl implements EventPrivateService {

    TransactionTemplate transactionTemplate;
    CategoryRepository categoryRepository;
    EventRepository eventRepository;
    UserClientHelper userClientHelper;
    RequestClientHelper requestClientHelper;
    StatClient statClient;

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление нового события пользователем с ID {}", userId);
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserIdOrFail(userId);
        return transactionTemplate.execute(status -> {
            Category category = categoryRepository.findById(newEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с ID " + newEventDto.getCategory() + " не найдена"));
            Event newEvent = EventMapper.toNewEvent(newEventDto, userId, category);
            eventRepository.save(newEvent);
            return EventMapper.toEventFullDto(newEvent, userShortDto, 0L, 0.0);
        });
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        log.info("Получение события с ID {} пользователем с ID {}", eventId, userId);
        Event event = transactionTemplate.execute(status -> eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено")));
        assert event != null;
        if (!Objects.equals(userId, event.getInitiatorId()))
            throw new ConflictException("Пользователь с ID " + userId + " не является организатором события с ID " + eventId, "Действие запрещено");
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(userId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(List.of(eventId));
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(List.of(eventId));
        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), ratingMap.get(eventId));
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя с ID {}", userId);
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(userId);
        List<Event> events = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());
            return eventRepository.findByInitiatorId(userId, pageable);
        });
        if (events == null || events.isEmpty()) return List.of();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(eventIds);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userShortDto,
                        confirmedRequestsMap.get(e.getId()),
                        ratingMap.get(e.getId())
                ))
                .toList();
    }

    @Override
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        log.info("Обновление события с ID {} пользователем с ID {}", eventId, userId);
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(userId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(List.of(eventId));
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(List.of(eventId));
        return transactionTemplate.execute(status -> {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
            if (!Objects.equals(userId, event.getInitiatorId()))
                throw new ConflictException("Пользователь с ID " + userId + " не является организатором события с ID " + eventId, "Действие запрещено");
            if (event.getState() != State.PENDING && event.getState() != State.CANCELED)
                throw new ConflictException("Только события в статусе 'ожидающее публикацию' или 'отмененное' могут быть изменены");
            if (updateEventDto.getEventDate() != null &&
                    updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
                throw new ConflictException("Дата события должна быть как минимум через 2 часа");
            if (updateEventDto.getCategory() != null) {
                Category category = categoryRepository.findById(updateEventDto.getCategory())
                        .orElseThrow(() -> new NotFoundException("Категория с ID " + updateEventDto.getCategory() + " не найдена"));
                event.setCategory(category);
            }
            if (updateEventDto.getTitle() != null) event.setTitle(updateEventDto.getTitle());
            if (updateEventDto.getAnnotation() != null) event.setAnnotation(updateEventDto.getAnnotation());
            if (updateEventDto.getDescription() != null) event.setDescription(updateEventDto.getDescription());
            if (updateEventDto.getLocation() != null)
                event.setLocation(LocationMapper.toEntity(updateEventDto.getLocation()));
            if (updateEventDto.getPaid() != null) event.setPaid(updateEventDto.getPaid());
            if (updateEventDto.getParticipantLimit() != null)
                event.setParticipantLimit(updateEventDto.getParticipantLimit());
            if (updateEventDto.getRequestModeration() != null)
                event.setRequestModeration(updateEventDto.getRequestModeration());
            if (updateEventDto.getEventDate() != null) event.setEventDate(updateEventDto.getEventDate());
            if (Objects.equals(updateEventDto.getStateAction(), StateAction.CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            } else if (Objects.equals(updateEventDto.getStateAction(), StateAction.SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            }
            eventRepository.save(event);
            return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), ratingMap.get(eventId));
        });
    }
}