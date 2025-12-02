package ru.practicum.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventHitDto;
import ru.practicum.dto.EventStatsResponseDto;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Выполнять только при запущенных Discovery and Config servers")
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
class StatsServiceImplTest {

    @Autowired
    StatsService statsService;

    @Autowired
    StatServiceRepository statServiceRepository;

    EventHitDto eventHitDto1;
    EventHitDto eventHitDto2;
    EventHitDto eventHitDto3;
    EventHitDto eventHitDto4;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        eventHitDto1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(1))
                .build();
        eventHitDto2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.2")
                .timestamp(now.minusHours(2))
                .build();
        eventHitDto3 = EventHitDto.builder()
                .app("app2")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(3))
                .build();
        eventHitDto4 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now.minusHours(4))
                .build();
    }

    @Test
    void hit_ShouldSaveEventSuccessfully() {
        statsService.hit(eventHitDto1);

        List<Stat> savedStats = statServiceRepository.findAll();
        assertEquals(1, savedStats.size());

        Stat savedStat = savedStats.get(0);
        assertEquals(eventHitDto1.getApp(), savedStat.getApp());
        assertEquals(eventHitDto1.getUri(), savedStat.getUri());
        assertEquals(eventHitDto1.getIp(), savedStat.getIp());
        assertEquals(eventHitDto1.getTimestamp(), savedStat.getTimestamp());
    }

    @Test
    void hit_ShouldSaveMultipleEventsSuccessfully() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);

        List<Stat> savedStats = statServiceRepository.findAll();
        assertEquals(3, savedStats.size());
    }

    @Test
    void getStats_ShouldThrowExceptionWhenStartIsAfterEnd() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statsService.getStats(start, end, null, false)
        );
    }

    @Test
    void getStats_ShouldReturnStatsWithUniqueIpWhenUrisAreEmptyAndUniqueIsTrue() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, true);

        assertEquals(3, result.size());

        EventStatsResponseDto[] statsArray = result.toArray(new EventStatsResponseDto[0]);

        assertTrue(statsArray[0].getHits() >= statsArray[1].getHits());
        assertTrue(statsArray[1].getHits() >= statsArray[2].getHits());
    }

    @Test
    void getStats_ShouldReturnStatsWithoutUniqueIpWhenUrisAreEmptyAndUniqueIsFalse() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, false);

        assertEquals(3, result.size());

        long totalHits = result.stream().mapToLong(EventStatsResponseDto::getHits).sum();
        assertEquals(4, totalHits);
    }

    @Test
    void getStats_ShouldReturnStatsWithUniqueIpForSpecificUris() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/1");

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, true);

        assertEquals(2, result.size());

        result.forEach(stat -> assertEquals("/events/1", stat.getUri()));
    }

    @Test
    void getStats_ShouldReturnStatsWithoutUniqueIpForSpecificUris() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/1", "/events/2");

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, false);

        assertEquals(3, result.size());

        result.forEach(stat ->
                assertTrue(stat.getUri().equals("/events/1") || stat.getUri().equals("/events/2"))
        );
    }

    @Test
    void getStats_ShouldReturnEmptyListWhenNoDataInTimeRange() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, false);

        assertTrue(result.isEmpty());
    }

    @Test
    void getStats_ShouldReturnEmptyListWhenNoMatchingUris() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> uris = List.of("/events/999");

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, uris, false);

        assertTrue(result.isEmpty());
    }

    @Test
    void getStats_ShouldHandleEmptyUrisList() {
        statsService.hit(eventHitDto1);
        statsService.hit(eventHitDto2);
        statsService.hit(eventHitDto3);
        statsService.hit(eventHitDto4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<String> emptyUris = List.of();

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, emptyUris, false);

        assertEquals(3, result.size());
    }

    @Test
    void getStats_ShouldCorrectlyCountUniqueIps() {
        EventHitDto hit1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();
        EventHitDto hit2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();
        EventHitDto hit3 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.now().minusHours(3))
                .build();
        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Collection<EventStatsResponseDto> uniqueResult = statsService.getStats(start, end, null, true);

        assertEquals(1, uniqueResult.size());
        EventStatsResponseDto uniqueStat = uniqueResult.iterator().next();
        assertEquals(2L, uniqueStat.getHits()); // 2 уникальных IP

        Collection<EventStatsResponseDto> allResult = statsService.getStats(start, end, null, false);

        assertEquals(1, allResult.size());
        EventStatsResponseDto allStat = allResult.iterator().next();
        assertEquals(3L, allStat.getHits()); // 3 общих хита
    }

    @Test
    void getStats_ShouldReturnResultsSortedByHitsDesc() {
        EventHitDto hit1 = EventHitDto.builder()
                .app("app1")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();
        EventHitDto hit2 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();
        EventHitDto hit3 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.2")
                .timestamp(LocalDateTime.now().minusHours(3))
                .build();
        EventHitDto hit4 = EventHitDto.builder()
                .app("app1")
                .uri("/events/2")
                .ip("192.168.1.3")
                .timestamp(LocalDateTime.now().minusHours(4))
                .build();
        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);
        statsService.hit(hit4);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Collection<EventStatsResponseDto> result = statsService.getStats(start, end, null, true);

        EventStatsResponseDto[] statsArray = result.toArray(new EventStatsResponseDto[0]);
        assertEquals(2, statsArray.length);

        assertEquals("/events/2", statsArray[0].getUri());
        assertEquals(3L, statsArray[0].getHits());

        assertEquals("/events/1", statsArray[1].getUri());
        assertEquals(1L, statsArray[1].getHits());
    }
}