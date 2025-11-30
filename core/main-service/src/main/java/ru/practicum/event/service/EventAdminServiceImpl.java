package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.JpaSpecifications;
import ru.practicum.event.repository.ViewRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestStatus;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)

public class EventAdminServiceImpl implements EventAdminService {

    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    RequestRepository requestRepository;
    ViewRepository viewRepository;

    @Override
    public List<EventFullDto> getAllEventsByParams(EventAdminParams params) {
        log.info("Получение всех событий с параметрами: {}", params);
        Pageable pageable = PageRequest.of(
                params.getFrom().intValue() / params.getSize().intValue(),
                params.getSize().intValue()
        );
        List<Event> events = eventRepository.findAll(JpaSpecifications.adminFilters(params), pageable).getContent();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
        return events.stream()
                .map(e -> EventMapper.toEventFullDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto) {
        log.info("Обновление события с идентификатором {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с указанным идентификатором не найдено"));
        if (updateEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с указанным идентификатором не найдена"));
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
                throw new ConflictException("Опубликованное событие не может быть отклонено");
            }
            event.setState(State.CANCELED);
        } else if (Objects.equals(updateEventDto.getStateAction(), StateAction.PUBLISH_EVENT)) {
            if (LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                throw new ConflictException("Событие должно начинаться не ранее, чем через час после публикации");
            }
            if (!Objects.equals(event.getState(), State.PENDING)) {
                throw new ConflictException("Событие должно находиться в состоянии ожидания для публикации");
            }
            event.setState(State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        eventRepository.save(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }
}