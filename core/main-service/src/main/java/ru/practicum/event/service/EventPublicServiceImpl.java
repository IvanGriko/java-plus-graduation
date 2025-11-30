package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventHitDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.View;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.JpaSpecifications;
import ru.practicum.event.repository.ViewRepository;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestStatus;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//@Transactional(readOnly = true)
//public class EventPublicServiceImpl implements EventPublicService {
//
//    StatClient statClient;
//    EventRepository eventRepository;
//    RequestRepository requestRepository;
//    ViewRepository viewRepository;
//
//    @Override
//    public List<EventShortDto> getAllEventsByParams(
//            EventParams params,
//            HttpServletRequest request
//    ) {
//        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
//            throw new BadRequestException("Начальная дата должна быть меньше конечной");
//        }
//        if (params.getRangeStart() == null) {
//            params.setRangeStart(LocalDateTime.now());
//        }
//        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
//        if (EventSort.VIEWS.equals(params.getEventSort())) {
//            sort = Sort.by(Sort.Direction.DESC, "views");
//        }
//        PageRequest pageRequest = PageRequest.of(
//                params.getFrom().intValue() / params.getSize().intValue(),
//                params.getSize().intValue(),
//                sort
//        );
//        Page<Event> events = eventRepository.findAll(JpaSpecifications.publicFilters(params), pageRequest);
//        List<Long> eventIds = events.stream().map(Event::getId).toList();
//        Map<Long, Long> confirmedRequestsMap = requestRepository.getConfirmedRequestsByEventIds(eventIds)
//                .stream()
//                .collect(Collectors.toMap(
//                        r -> (Long) r[0],
//                        r -> (Long) r[1]
//                ));
//        Map<Long, Long> viewsMap = viewRepository.countsByEventIds(eventIds)
//                .stream()
//                .collect(Collectors.toMap(
//                        r -> (Long) r[0],
//                        r -> (Long) r[1]
//                ));
//        statClient.hit(EventHitDto.builder()
//                .ip(request.getRemoteAddr())
//                .uri(request.getRequestURI())
//                .app("ewm-main-service")
//                .timestamp(LocalDateTime.now())
//                .build());
//        return events.stream()
//                .map(e -> EventMapper.toEventShortDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = false)
//    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
//        log.info("Получение события с id={}", eventId);
//        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
//                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
//        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
//        Long views = viewRepository.countByEventId(eventId);
//        if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
//            View view = View.builder()
//                    .event(event)
//                    .ip(request.getRemoteAddr())
//                    .build();
//            viewRepository.save(view);
//        }
//        statClient.hit(EventHitDto.builder()
//                .ip(request.getRemoteAddr())
//                .uri(request.getRequestURI())
//                .app("ewm-main-service")
//                .timestamp(LocalDateTime.now())
//                .build());
//        return EventMapper.toEventFullDto(event, confirmedRequests, views);
//    }
//}

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {

    StatClient statClient;
    EventRepository eventRepository;
    RequestRepository requestRepository;
    ViewRepository viewRepository;

    @Override
    public List<EventShortDto> getAllEventsByParams(EventParams params, HttpServletRequest request) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new BadRequestException("Конечная дата должна быть позже начальной", "Ошибка валидации");
        }
        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());
        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
        if (EventSort.VIEWS.equals(params.getEventSort())) sort = Sort.by(Sort.Direction.DESC, "views");
        PageRequest pageRequest = PageRequest.of(params.getFrom().intValue() / params.getSize().intValue(),
                params.getSize().intValue(), sort);
        Page<Event> events = eventRepository.findAll(JpaSpecifications.publicFilters(params), pageRequest);
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
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmedRequestsMap.get(e.getId()), viewsMap.get(e.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        Long views = viewRepository.countByEventId(eventId);
        if (!viewRepository.existsByEventIdAndIp(eventId, request.getRemoteAddr())) {
            View view = View.builder()
                    .event(event)
                    .ip(request.getRemoteAddr())
                    .build();
            viewRepository.save(view);
        }
        statClient.hit(EventHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now())
                .build());
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }
}