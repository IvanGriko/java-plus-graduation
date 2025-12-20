package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.EventStatsResponseDto;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatServiceRepository extends JpaRepository<Stat, Long> {

    @Query("""
            SELECT NEW ru.practicum.dto.EventStatsResponseDto(s.app, s.uri, COUNT(DISTINCT s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN ?1 AND ?2
            GROUP BY s.app, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<EventStatsResponseDto> findAllByTimestampBetweenStartAndEndWithUniqueIp(LocalDateTime start,
                                                                                 LocalDateTime end);

    @Query("""
            SELECT NEW ru.practicum.dto.EventStatsResponseDto(s.app, s.uri, COUNT(s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN ?1 AND ?2
            GROUP BY s.app, s.uri
            ORDER BY COUNT(s.ip) DESC
            """)
    List<EventStatsResponseDto> findAllByTimestampBetweenStartAndEndWhereIpNotUnique(LocalDateTime start,
                                                                                     LocalDateTime end);

    @Query("""
            SELECT NEW ru.practicum.dto.EventStatsResponseDto(s.app, s.uri, COUNT(DISTINCT s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN ?1 AND ?2 AND s.uri IN ?3
            GROUP BY s.app, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<EventStatsResponseDto> findAllByTimestampBetweenStartAndEndWithUrisUniqueIp(LocalDateTime start,
                                                                                     LocalDateTime end,
                                                                                     List<String> uris);

    @Query("""
            SELECT NEW ru.practicum.dto.EventStatsResponseDto(s.app, s.uri, COUNT(s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN ?1 AND ?2 AND s.uri IN ?3
            GROUP BY s.app, s.uri
            ORDER BY COUNT(s.ip) DESC
            """)
    List<EventStatsResponseDto> findAllByTimestampBetweenStartAndEndWithUrisIpNotUnique(LocalDateTime start,
                                                                                        LocalDateTime end,
                                                                                        List<String> uris);

}