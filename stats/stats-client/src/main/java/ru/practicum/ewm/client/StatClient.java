package ru.practicum.ewm.client;

import ru.practicum.dto.EventHitDto;
import ru.practicum.dto.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatClient {

    Collection<EventStatsResponseDto> stats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    );

    void hit(EventHitDto eventHitDto);

}
