package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientAbstractHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.dto.event.EventInteractionDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.ParticipationRequestStatus;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dal.Request;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

    TransactionTemplate transactionTemplate;
    RequestRepository requestRepository;
    UserClientHelper userClientHelper;
    EventClientAbstractHelper eventClientHelper;
    StatClient statClient;

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Пользователь с ID {} отправляет заявку на участие в событии с ID {}", userId, eventId);
        userClientHelper.fetchUserShortDtoByUserIdOrFail(userId);
        EventInteractionDto eventDto = eventClientHelper.fetchEventInteractionByIdOrFail(eventId);
        ParticipationRequestDto result = transactionTemplate.execute(status -> {
            if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
                log.error("Пользователь с ID {} пытается подать повторную заявку на событие с ID {}",
                        userId, eventId);
                throw new ConflictException("Пользователь уже отправил заявку на это событие",
                        "Запрещённое действие");
            }
            if (Objects.equals(userId, eventDto.getInitiatorId())) {
                log.error("Пользователь с ID {} пытается подать заявку на своё собственное событие с ID {}",
                        userId, eventId);
                throw new ConflictException("Организатор события не может подавать заявку на своё событие",
                        "Запрещённое действие");
            }
            if (eventDto.getState() != State.PUBLISHED) {
                log.error("Пользователь с ID {} пытается подать заявку на неопубликованное событие с ID {}",
                        userId, eventId);
                throw new ConflictException("Заявка на участие невозможна, так как событие не опубликовано",
                        "Запрещённое действие");
            }
            long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            if (eventDto.getParticipantLimit() > 0 && confirmedRequestCount >= eventDto.getParticipantLimit()) {
                log.error("Пользователь с ID {} пытается подать заявку на событие с ID {}, но достигнуто ограничение участников",
                        userId, eventId);
                throw new ConflictException("Участие ограничено, достигнут максимальный предел заявок",
                        "Запрещённое действие");
            }
            ParticipationRequestStatus newRequestStatus = ParticipationRequestStatus.PENDING;
            if (!eventDto.getRequestModeration()) {
                newRequestStatus = ParticipationRequestStatus.CONFIRMED;
            }
            if (eventDto.getParticipantLimit().equals(0L)) {
                newRequestStatus = ParticipationRequestStatus.CONFIRMED;
            }
            Request newRequest = Request.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(newRequestStatus)
                    .created(LocalDateTime.now())
                    .build();
            requestRepository.save(newRequest);
            return RequestMapper.toDto(newRequest);
        });
        statClient.sendRegister(userId, eventId);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Пользователь с ID {} отменяет заявку с ID {}", userId, requestId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с ID " + requestId + " не найдена"));
        if (!Objects.equals(request.getRequesterId(), userId)) {
            log.error("Пользователь с ID {} пытается отменить заявку с ID {}, не являясь её владельцем",
                    userId, requestId);
            throw new ConflictException("Пользователь может отменить только собственную заявку",
                    "Запрещённое действие");
        }
        request.setStatus(ParticipationRequestStatus.CANCELED);
        requestRepository.save(request);
        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ParticipationRequestDto> findRequesterRequests(Long userId) {
        log.info("Получение заявок пользователя с ID {}", userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие с ID {} пользователем с ID {}", eventId, userId);
        EventInteractionDto eventDto = eventClientHelper.fetchEventInteractionByIdOrFail(eventId);
        if (!Objects.equals(userId, eventDto.getInitiatorId())) {
            log.error("Пользователь с ID {} не является организатором события с ID {}", userId, eventId);
            throw new ConflictException("Пользователь не является организатором события", "Запрещённое действие");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    ) {
        log.info("Модерация заявок на событие с ID {} пользователем с ID {}", eventId, userId);
        EventInteractionDto eventDto = eventClientHelper.fetchEventInteractionByIdOrFail(eventId);
        if (!Objects.equals(userId, eventDto.getInitiatorId())) {
            log.error("Пользователь с ID {} не является организатором события с ID {}", userId, eventId);
            throw new ConflictException("Пользователь не является организатором события", "Запрещённое действие");
        }
        if (eventDto.getParticipantLimit() < 1 || !eventDto.getRequestModeration())
            return new EventRequestStatusUpdateResultDto();
        return transactionTemplate.execute(status -> {
            List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
            for (Request request : requests) {
                if (!Objects.equals(request.getStatus(), ParticipationRequestStatus.PENDING)) {
                    log.error("Заявка с ID {} не находится в состоянии ожидания", request.getId());
                    throw new ConflictException("Можно изменить статус только заявок в состоянии ожидания",
                            "Запрещённое действие");
                }
            }
            List<Long> requestsToConfirm = new ArrayList<>();
            List<Long> requestsToReject = new ArrayList<>();
            if (Objects.equals(updateRequestDto.getStatus(), ParticipationRequestStatus.CONFIRMED)) {
                long confirmedRequestCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
                if (confirmedRequestCount >= eventDto.getParticipantLimit()) {
                    log.error("Уже достигнут лимит участников для события с ID {}", eventId);
                    throw new ConflictException("Уже достигнут лимит участников для события", "Запрещённое действие");
                } else if (updateRequestDto.getRequestIds().size() < eventDto.getParticipantLimit() - confirmedRequestCount) {
                    requestsToConfirm = updateRequestDto.getRequestIds();
                    requestRepository.updateStatusByIds(requestsToConfirm, ParticipationRequestStatus.CONFIRMED);
                } else {
                    long freeSeats = eventDto.getParticipantLimit() - confirmedRequestCount;
                    requestsToConfirm = updateRequestDto.getRequestIds().stream()
                            .limit(freeSeats)
                            .toList();
                    requestsToReject = updateRequestDto.getRequestIds().stream()
                            .skip(freeSeats)
                            .toList();
                    requestRepository.updateStatusByIds(requestsToConfirm, ParticipationRequestStatus.CONFIRMED);
                    requestRepository.setStatusToRejectForAllPending(eventId);
                }
            } else if (updateRequestDto.getStatus() == ParticipationRequestStatus.REJECTED) {
                requestsToReject = updateRequestDto.getRequestIds();
                requestRepository.updateStatusByIds(requestsToReject, ParticipationRequestStatus.REJECTED);
            } else {
                log.error("Статус заявки {} недопустим", updateRequestDto.getStatus());
                throw new ConflictException("Допустимы только статусы CONFIRMED и REJECTED", "Запрещённое действие");
            }
            EventRequestStatusUpdateResultDto resultDto = new EventRequestStatusUpdateResultDto();
            List<ParticipationRequestDto> confirmedRequests = requestRepository.findAllById(requestsToConfirm).stream()
                    .map(RequestMapper::toDto)
                    .toList();
            resultDto.setConfirmedRequests(confirmedRequests);
            List<ParticipationRequestDto> rejectedRequests = requestRepository.findAllById(requestsToReject).stream()
                    .map(RequestMapper::toDto)
                    .toList();
            resultDto.setRejectedRequests(rejectedRequests);
            return resultDto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        log.info("Получение подтвержденных заявок для событий с ID {}", eventIds);
        if (eventIds == null || eventIds.isEmpty()) return Map.of();
        return requestRepository.getConfirmedRequestsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    @Transactional(readOnly = true)
    public String checkParticipation(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId, ParticipationRequestStatus.CONFIRMED)) {
            return "true";
        }
        throw new NotFoundException("Не найдено подтверждённых заявок пользователя с ID " + userId
                + " на событие с ID " + eventId);
    }

}