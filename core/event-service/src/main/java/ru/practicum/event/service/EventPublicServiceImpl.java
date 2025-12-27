package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.EventHitDto;
import ru.practicum.client.RequestClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.*;
import ru.practicum.event.filter.EventDynamicFilters;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
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
            PageRequest pageRequest = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
            return eventRepository.findAll(EventDynamicFilters.buildPublicFilter(params), pageRequest).getContent();
        });
        if (events == null) return List.of();
        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(eventIds);
        if (params.getOnlyAvailable() == true && !confirmedRequestsMap.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        if (Objects.equals(e.getParticipantLimit(), 0L)) return true;
                        Long confirmedRequests = confirmedRequestsMap.get(e.getId());
                        if (confirmedRequests == null) return true;
                        return confirmedRequests < e.getParticipantLimit();
                    }).toList();
        }
        List<EventShortDto> unsortedResult = events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.get(e.getInitiatorId()),
                        confirmedRequestsMap.get(e.getId()),
                        ratingMap.get(e.getId())
                ))
                .toList();
        Comparator<EventShortDto> resultComparator = switch (params.getEventSort()) {
            case VIEWS, RATING -> Comparator.comparing(EventShortDto::getRating).reversed();
            default -> Comparator.comparing(EventShortDto::getEventDate).reversed();
        };
        return unsortedResult.stream()
                .sorted(resultComparator)
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId, HttpServletRequest request) {
        log.info("Получение события с ID {}", eventId);
        Event event = transactionTemplate.execute(status -> {
            return eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                    .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        });
        UserShortDto userShortDto = userClientHelper.fetchUserShortDtoByUserId(event.getInitiatorId());
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(List.of(eventId));
        Map<Long, Double> ratingMap = statClient.getRatingsByEventIdList(List.of(eventId));
        statClient.sendView(userId, eventId);
        return EventMapper.toEventFullDto(event, userShortDto, confirmedRequestsMap.get(eventId), ratingMap.get(eventId));
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

//    @Override
//    public Collection<EventShortDto> getRecommendations(Long userId, Integer size) {
//        log.info("Получение рекомендаций для пользователя с ID {}", userId);
//        Map<Long, Double> recommendationMap = statClient.getUserRecommendations(userId, size);
//        if (recommendationMap.isEmpty()) return List.of();
//        List<Event> events = transactionTemplate.execute(status -> {
//            return eventRepository.findAllById(recommendationMap.keySet());
//        });
//        if (events == null || events.isEmpty()) return List.of();
//        Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
//        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
//        List<Long> eventIds = events.stream().map(Event::getId).toList();
//        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
//        return events.stream()
//                .map(e -> EventMapper.toEventShortDto(
//                        e,
//                        userMap.get(e.getInitiatorId()),
//                        confirmedRequestsMap.get(e.getId()),
//                        recommendationMap.get(e.getId())
//                ))
//                .sorted(Comparator.comparing(EventShortDto::getRating).reversed())
//                .toList();
//    }

    @Override
    public Collection<EventShortDto> getRecommendations(Long userId, Integer size) {
        log.info("Получение рекомендаций для пользователя с ID {}", userId);
        Map<Long, Double> recommendationMap = statClient.getUserRecommendations(userId, size);
        if (recommendationMap.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> eventIds = recommendationMap.keySet();
        List<Event> events = transactionTemplate.execute((TransactionCallback<List<Event>>) status -> {
            return eventRepository.findAllById(eventIds);
        });
        if (CollectionUtils.isEmpty(events)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        Map<Long, Long> confirmedRequestsMap = requestClientHelper.fetchConfirmedRequestsCountByEventIds(eventIds);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto()),
                        confirmedRequestsMap.getOrDefault(e.getId(), 0L),
                        recommendationMap.get(e.getId())))
                .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        log.info("Отправка информации о лайке пользователя с ID {} к событию с ID {}", userId, eventId);
        if (!requestClientHelper.passedParticipationCheck(userId, eventId))
            throw new BadRequestException("Пользователь с ID " + userId + " пытается лайкнуть событие c ID " + eventId
                    + ", в котором не участвовал");
        return statClient.sendLike(userId, eventId);
    }
}