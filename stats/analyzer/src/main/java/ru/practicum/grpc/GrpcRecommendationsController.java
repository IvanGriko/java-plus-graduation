package ru.practicum.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.collector.RecommendationsControllerGrpc;
import ru.practicum.grpc.similarity.reports.InteractionsCountRequestProto;
import ru.practicum.grpc.similarity.reports.RecommendedEventProto;
import ru.practicum.grpc.similarity.reports.SimilarEventsRequestProto;
import ru.practicum.grpc.similarity.reports.UserPredictionsRequestProto;
import ru.practicum.service.SimilarityReportService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcRecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final SimilarityReportService similarityReportService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        log.info("Запрашиваем рекомендации для пользователя");
        try {
            List<RecommendedEventProto> recommendations = similarityReportService.getRecommendationsForUser(request);
            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.info("Передали {} рекомендуемых событий пользователю", recommendations.size());
        } catch (Exception e) {
            log.error("Ошибка при расчете рекомендаций: {}", e.getMessage(), e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
            );
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        log.info("Получаем похожие события");
        try {
            List<RecommendedEventProto> similarEvents = similarityReportService.getSimilarEvents(request);
            similarEvents.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.info("Передали {} похожих событий", similarEvents.size());
        } catch (Exception e) {
            log.error("Ошибка при поиске похожих событий: {}", e.getMessage(), e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
            );
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        log.info("Запрашиваем количество взаимодействий с событием");
        try {
            List<RecommendedEventProto> interactions = similarityReportService.getInteractionsCount(request);
            interactions.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.info("Передали {} записей о взаимодействии", interactions.size());
        } catch (Exception e) {
            log.error("Ошибка при подсчете взаимодействий: {}", e.getMessage(), e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
            );
        }
    }
}