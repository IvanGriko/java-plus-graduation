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

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcRecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final SimilarityReportService similarityReportService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            log.info("Запрошено формирование рекомендаций для пользователя с ID {}", request.getUserId());
            List<RecommendedEventProto> result = similarityReportService.getRecommendationsForUser(request);
            result.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при формировании рекомендаций для пользователя.", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e)));
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            log.info("Запрошены похожие мероприятия для события с ID {}", request.getEventId());
            List<RecommendedEventProto> result = similarityReportService.getSimilarEvents(request);
            result.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при поиске похожих мероприятий.", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e)));
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            log.info("Запрошен отчет о количестве взаимодействий для списка событий");
            List<RecommendedEventProto> result = similarityReportService.getInteractionsCount(request);
            result.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при расчете количества взаимодействий.", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e)));
        }
    }
}

//@GrpcService
//@RequiredArgsConstructor
//public class GrpcRecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
//
//    private final SimilarityReportService similarityReportService;
//
//    @Override
//    public void getRecommendationsForUser(
//            UserPredictionsRequestProto request,
//            StreamObserver<RecommendedEventProto> responseObserver
//    ) {
//        try {
//            List<RecommendedEventProto> result = similarityReportService.getRecommendationsForUser(request);
//            result.forEach(responseObserver::onNext);
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(
//                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
//            );
//        }
//    }
//
//    @Override
//    public void getSimilarEvents(
//            SimilarEventsRequestProto request,
//            StreamObserver<RecommendedEventProto> responseObserver
//    ) {
//        try {
//            List<RecommendedEventProto> result = similarityReportService.getSimilarEvents(request);
//            result.forEach(responseObserver::onNext);
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(
//                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
//            );
//        }
//    }
//
//    @Override
//    public void getInteractionsCount(
//            InteractionsCountRequestProto request,
//            StreamObserver<RecommendedEventProto> responseObserver
//    ) {
//        try {
//            List<RecommendedEventProto> result = similarityReportService.getInteractionsCount(request);
//            result.forEach(responseObserver::onNext);
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(
//                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
//            );
//        }
//    }
//}