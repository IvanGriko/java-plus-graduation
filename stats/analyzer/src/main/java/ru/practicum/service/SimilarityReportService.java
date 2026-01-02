package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.grpc.similarity.reports.InteractionsCountRequestProto;
import ru.practicum.grpc.similarity.reports.RecommendedEventProto;
import ru.practicum.grpc.similarity.reports.SimilarEventsRequestProto;
import ru.practicum.grpc.similarity.reports.UserPredictionsRequestProto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimilarityReportService {

    UserActionRepository userActionRepository;
    EventSimilarityRepository eventSimilarityRepository;

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();
        log.info("Генерация рекомендаций для пользователя {}", userId);
        // Поиск последних просмотренных событий пользователя
        List<Long> recentUserEventIds = userActionRepository.findRecentEventIdListByUserId(userId, maxResults);
        log.debug("Последние события пользователя {}: {}", userId, recentUserEventIds);
        // Поиск похожих событий, которые пользователь ещё не видел
        List<Long> similarEventIds = eventSimilarityRepository.findSimilarByEventIdListNotSeenByUser(
                        userId,
                        recentUserEventIds,
                        maxResults
                ).stream()
                .map(o -> ((Number) o[0]).longValue())
                .toList();
        log.debug("Похожие события, не увиденные пользователем {}: {}", userId, similarEventIds);
        // Расчёт среднего взвешенного рейтинга для найденных событий
        List<Object[]> averageResult = eventSimilarityRepository.findWeightedAverageListByEventIdList(userId, similarEventIds);
        log.debug("Средневзвешенные оценки найдены: {}", averageResult);
        return averageResult.stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(((Number) o[0]).longValue())
                        .setScore(((Number) o[1]).doubleValue())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();
        log.info("Поиск похожих событий для события {} и пользователя {}", eventId, userId);
        // Нахождение похожих событий, не видимых ранее данным пользователем
        List<Object[]> similarEvents = eventSimilarityRepository.findSimilarByEventIdListNotSeenByUser(
                userId,
                List.of(eventId),
                maxResults
        );
        log.debug("Похожие события для события {}: {}", eventId, similarEvents);
        return similarEvents.stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(((Number) o[0]).longValue())
                        .setScore(((Number) o[1]).doubleValue())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIdList = request.getEventIdList();
        log.info("Расчет числа взаимодействий для списка событий: {}", eventIdList);
        // Сбор статистики по количеству взаимодействий
        Map<Long, Double> sumMap = userActionRepository.findWeightSumListByEventIdList(eventIdList)
                .stream()
                .collect(Collectors.toMap(
                        o -> ((Number) o[0]).longValue(),
                        o -> ((Number) o[1]).doubleValue()
                ));
        log.debug("Количество взаимодействий найдено: {}", sumMap);
        return eventIdList.stream()
                .map(id -> RecommendedEventProto.newBuilder()
                        .setEventId(id)
                        .setScore(sumMap.computeIfAbsent(id, k -> 0.0))
                        .build())
                .toList();
    }
}