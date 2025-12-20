package ru.practicum.ewm.client;

import ru.practicum.dto.EventHitDto;
import ru.practicum.dto.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StatClient {

    Collection<EventStatsResponseDto> stats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    );

    void hit(EventHitDto eventHitDto);

    String sendView(Long userId, Long eventId);

    String sendRegister(Long userId, Long eventId);

    String sendLike(Long userId, Long eventId);

    Map<Long, Double> getUserRecommendations(Long userId, Integer size);

    Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList);

}
