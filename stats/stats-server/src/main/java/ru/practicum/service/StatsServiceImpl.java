package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.model.Stat;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.repository.StatsServiceRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsServiceImpl implements StatsService {

    StatsServiceRepository statsServiceRepository;

    @Transactional
    public void hit(EventHitDto eventHitDto) {
        log.info("Hit - invoked");
        Stat stat = statsServiceRepository.save(StatMapper.INSTANCE.toStat(eventHitDto));
        log.info("Hit - stat saved successfully - {}", stat);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<EventStatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean isUnique) {
        log.info("getStats - invoked");
        if (start.isAfter(end)) {
            log.error("Error occurred: The start date cannot be later than the end date");
            throw new IllegalArgumentException("The start date cannot be later than the end date");
        }
        if (uris == null || uris.isEmpty()) {
            if (isUnique) {
                log.info("getStats - success - unique = true, uris empty");
                return statsServiceRepository.findAllByTimestampBetweenStartAndEndWithUniqueIp(start, end);
            } else {
                log.info("getStats - success - unique = false, uris empty");
                return statsServiceRepository.findAllByTimestampBetweenStartAndEndWhereIpNotUnique(start, end);
            }
        } else {
            if (isUnique) {
                log.info("getStats - success - unique = true, uris not empty");
                return statsServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(start, end, uris);
            } else {
                log.info("getStats - success - unique = false, uris not empty");
                return statsServiceRepository.findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(start, end, uris);
            }
        }
    }
}