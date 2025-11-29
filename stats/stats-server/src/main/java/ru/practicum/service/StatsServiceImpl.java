package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventHitDto;
import ru.practicum.dto.EventStatsResponseDto;
import ru.practicum.model.Stat;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatServiceRepository statServiceRepository;

    @Transactional
    public void hit(EventHitDto eventHitDto) {
        log.info("Запуск обработки события HIT");
        Stat stat = StatMapper.INSTANCE.toStat(eventHitDto);
        Stat savedStat = statServiceRepository.save(stat);
        log.info("HIT успешно сохранён - {}", savedStat);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventStatsResponseDto> getStats(LocalDateTime start,
                                                      LocalDateTime end,
                                                      List<String> uris,
                                                      boolean isUnique)
    {
        log.info("Запрос статистики за период: {} - {}", start, end);
        validateInterval(start, end);
        return fetchResults(start, end, uris, isUnique);
    }

    private void validateInterval(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.error("Ошибка: Начальная дата позже конечной");
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной");
        }
    }

    private Collection<EventStatsResponseDto> fetchResults(LocalDateTime start,
                                                           LocalDateTime end,
                                                           List<String> uris,
                                                           boolean isUnique)
    {
        if (uris == null || uris.isEmpty()) {
            return isUnique ? statServiceRepository.findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end)
                    : statServiceRepository.findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end);
        } else {
            return isUnique ? statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris)
                    : statServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris);
        }
    }
}