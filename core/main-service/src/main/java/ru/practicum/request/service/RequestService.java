package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.ParticipationRequestStatus;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class RequestService {

    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Transactional(readOnly = false)
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Пользователь пытается создать повторный запрос", "Запрещённое действие");
        }
        if (Objects.equals(requester.getId(), event.getInitiator().getId())) {
            throw new ConflictException("Пользователь пытается зарегистрироваться на своё собственное мероприятие", "Запрещённое действие");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Пользователь пытается зарегистрироваться на неопубликованное мероприятие", "Запрещённое действие");
        }
        long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequestCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников мероприятия", "Запрещённое действие");
        }
        ParticipationRequestStatus newRequestStatus = ParticipationRequestStatus.PENDING;
        if (!event.getRequestModeration()) newRequestStatus = ParticipationRequestStatus.CONFIRMED;
        if (Objects.equals(event.getParticipantLimit(), 0L)) newRequestStatus = ParticipationRequestStatus.CONFIRMED;
        Request newRequest = Request.builder()
                .requester(requester)
                .event(event)
                .status(newRequestStatus)
                .created(LocalDateTime.now())
                .build();
        requestRepository.save(newRequest);
        return RequestMapper.toDto(newRequest);
    }

    @Transactional(readOnly = false)
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки участником с id {} на запрос с id {}", userId, requestId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным идентификатором не найден"));
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с указанным идентификатором не найдена"));
        existingRequest.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(existingRequest);
        return RequestMapper.toDto(existingRequest);
    }

    public Collection<ParticipationRequestDto> findRequesterRequests(Long userId) {
        log.info("Получение заявок пользователя с id {}", userId);
        return requestRepository.findByRequesterId(userId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    public Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие с id {} пользователем с id {}", eventId, userId);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным идентификатором не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с указанным идентификатором не найдено"));
        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("Пользователь не является организатором данного события", "Запрещённое действие");
        }
        return requestRepository.findByEventId(eventId).stream()
                .filter(Objects::nonNull)
                .map(RequestMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = false)
    public EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        log.info("Обработка модерации заявок на событие с id {} пользователем с id {}", eventId, userId);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным идентификатором не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с указанным идентификатором не найдено"));
        if (!Objects.equals(initiator.getId(), event.getInitiator().getId())) {
            throw new ConflictException("Пользователь не является организатором данного события", "Запрещённое действие");
        }
        if (event.getParticipantLimit() <= 0 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResultDto();
        }
        List<Request> fetchedRequests = requestRepository.findAllById(updateRequestDto.getRequestIds());
        List<Long> confirmedRequests = new ArrayList<>();
        List<Long> rejectedRequests = new ArrayList<>();
        for (Request request : fetchedRequests) {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Заявка " + request.getId() + " уже прошла рассмотрение", "Запрещённое действие");
            }
        }
        switch (updateRequestDto.getStatus()) {
            case CONFIRMED:
                long currentConfirmedCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
                if (currentConfirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("Максимальное количество участников уже зарегистрировано", "Запрещённое действие");
                }
                long availableSlots = Math.min(updateRequestDto.getRequestIds().size(), event.getParticipantLimit() - (int) currentConfirmedCount);
                confirmedRequests.addAll(updateRequestDto.getRequestIds().subList(0, (int) availableSlots));
                rejectedRequests.addAll(updateRequestDto.getRequestIds().subList((int) availableSlots, updateRequestDto.getRequestIds().size()));
                requestRepository.updateStatusByIds(confirmedRequests, ParticipationRequestStatus.CONFIRMED);
                requestRepository.setStatusToRejectForAllPending(eventId);
                break;
            case REJECTED:
                rejectedRequests.addAll(updateRequestDto.getRequestIds());
                requestRepository.updateStatusByIds(rejectedRequests, ParticipationRequestStatus.REJECTED);
                break;
            default:
                throw new ConflictException("Поддерживаются только статусы CONFIRMED и REJECTED", "Запрещённое действие");
        }
        EventRequestStatusUpdateResultDto resultDto = new EventRequestStatusUpdateResultDto();
        List<ParticipationRequestDto> confirmedRequestsMapped = requestRepository.findAllById(confirmedRequests).stream()
                .map(RequestMapper::toDto)
                .toList();
        resultDto.setConfirmedRequests(confirmedRequestsMapped);
        List<ParticipationRequestDto> rejectedRequestsMapped = requestRepository.findAllById(rejectedRequests).stream()
                .map(RequestMapper::toDto)
                .toList();
        resultDto.setRejectedRequests(rejectedRequestsMapped);
        return resultDto;
    }
}
