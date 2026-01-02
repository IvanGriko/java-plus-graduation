package ru.practicum.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties("my-area-guide")
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomProperties {

    Kafka kafka = new Kafka();
    Aggregator aggregator = new Aggregator();

    @Getter
    @Setter
    public static class Kafka {
        private String userActionTopic = "user-actions";       // Тема для действий пользователей
        private String eventsSimilarityTopic = "events-similarity"; // Тема для сходства событий
    }

    @Getter
    @Setter
    public static class Aggregator {
        private final Weights weights = new Weights();
        private String minimumSumAlgorithm = "optimized";      // Алгоритм минимальных сумм
    }

    @Getter
    @Setter
    public static class Weights {
        private String like = "0.9";                          // Вес лайков
        private String register = "0.7";                      // Вес регистраций
        private String view = "0.3";                          // Вес просмотров

        public BigDecimal ofUserAction(UserActionAvro userActionAvro) {
            return switch (userActionAvro.getActionType()) {
                case LIKE -> new BigDecimal(like);           // Вес лайка
                case REGISTER -> new BigDecimal(register);   // Вес регистрации
                default -> new BigDecimal(view);              // По умолчанию вес просмотра
            };
        }
    }
}