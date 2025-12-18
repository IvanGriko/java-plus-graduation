package ru.practicum.request.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.Collection;
import java.util.Map;

public interface RequestService {

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Collection<ParticipationRequestDto> findRequesterRequests(Long userId);

    Collection<ParticipationRequestDto> findEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto moderateRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto updateRequestDto
    );

    @Transactional(readOnly = true)
    Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds);
}
