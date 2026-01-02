package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.category.dal.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.filter.EventDynamicFilters;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventAdminServiceImpl implements EventAdminService {

    TransactionTemplate transactionTemplate;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    RequestClientHelper requestClientHelper;
    UserClientHelper userClientHelper;
    StatClient statClient;

    @Override
    public List<EventFullDto> getAllEventsByParams(EventAdminParams params) {
        log.info("Начинается получение всех событий по административным критериям");
        Page<Event> events = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
            return eventRepository.findAll(EventDynamicFilters.buildAdminFilter(params), pageable);
        });
        if (events == null) return List.of();
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(eventIds);
        return events.stream()
                .map(e -> EventMapper.toEventFullDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        ratingMap.get(e.getId())
                ))
                .toList();
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto) {
        log.info("Начинается обновление события с ID {}", eventId);
        Long initiatorId = transactionTemplate.execute(status -> eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"))
                .getInitiatorId());
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(initiatorId);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(List.of(eventId));
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(List.of(eventId));
        return transactionTemplate.execute(status -> {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
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
            if (Objects.equals(updateEventDto.getStateAction(), StateAction.REJECT_EVENT)) {
                if (Objects.equals(event.getState(), State.PUBLISHED)) {
                    log.error("Событие с состоянием PUBLISHED не может быть отменено");
                    throw new ConflictException("Событие с опубликованным статусом не может быть отменено");
                }
                event.setState(State.CANCELED);
            } else if (Objects.equals(updateEventDto.getStateAction(), StateAction.PUBLISH_EVENT)) {
                if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                    log.error("Время события должно быть как минимум на 1 час позднее момента публикации");
                    throw new ConflictException("Время события должно быть как минимум на 1 час позднее момента публикации");
                }
                if (!Objects.equals(event.getState(), State.PENDING)) {
                    log.error("Событие должно находиться в состоянии ожидания публикации");
                    throw new ConflictException("Событие должно находиться в ожидании публикации");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            eventRepository.save(event);
            return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), ratingMap.get(eventId));
        });
    }
}