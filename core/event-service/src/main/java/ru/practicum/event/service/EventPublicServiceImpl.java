package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.dto.EventHitDto;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.*;
import ru.practicum.event.filter.EventDynamicFilters;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.ViewRepository;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPublicServiceImpl implements EventPublicService {

    TransactionTemplate transactionTemplate;
    EventRepository eventRepository;
    ViewRepository viewRepository;
    UserClientHelper userClientHelper;
    RequestClientHelper requestClientHelper;
    StatClient statClient;

    @Override
    public List<EventShortDto> getAllEventsByParams(EventParams params, HttpServletRequest request) {
        log.info("Получение всех событий по публичным критериям");
        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Диапазон дат неверен: конец диапазона раньше начала");
            throw new BadRequestException("Дата конца диапазона должна быть позже начальной даты");
        }
        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now());
            params.setRangeEnd(null);
        }
        List<Event> events = transactionTemplate.execute(status -> {
            Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
            if (EventSort.VIEWS.equals(params.getEventSort())) sort = Sort.by(Sort.Direction.DESC, "views");
            PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
            return eventRepository.findAll(EventDynamicFilters.buildPublicFilter(params), pageRequest).getContent();
        });
        if (events == null) return List.of();
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
        if (params.getOnlyAvailable() == true && !confirmedRequestsMap.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        if (Objects.equals(e.getParticipantLimit(), 0L)) return true;
                        Long confirmedRequests = confirmedRequestsMap.get(e.getId());
                        if (confirmedRequests == null) return true;
                        return confirmedRequests < e.getParticipantLimit();
                    }).toList();
        }
        Map<Long, Long> viewsMap = Optional.ofNullable(
                transactionTemplate.execute(status -> {
                    return viewRepository.countsByEventIds(eventIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    r -> (Long) r[0],
                                    r -> (Long) r[1]
                            ));
                })
        ).orElse(Map.of());
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        viewsMap.get(e.getId())
                ))
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        log.info("Получение события с ID {}", eventId);
        Event event = transactionTemplate.execute(status -> {
            return eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                    .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        });
        Long views = transactionTemplate.execute(status -> {
            Long viewsBefore = viewRepository.countByEventId(eventId);
            if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
                View view = View.builder()
                        .event(event)
                        .ip(request.getRemoteAddr())
                        .build();
                viewRepository.save(view);
            }
            return viewsBefore;
        });
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(event.getInitiatorId());
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(List.of(eventId));
        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventCommentDto getEventCommentDto(Long eventId) {
        log.info("Получение комментария события с ID {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        return EventMapper.toEventComment(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventCommentDto> getEventCommentDtoList(Collection<Long> ids) {
        log.info("Получение комментариев для событий с ID {}", ids);
        if (ids == null || ids.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllById(ids);
        return events.stream()
                .map(EventMapper::toEventComment)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventInteractionDto getEventInteractionDto(Long eventId) {
        log.info("Получение статистики взаимодействия для события с ID {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        return EventMapper.toInteractionDto(event);
    }

}
