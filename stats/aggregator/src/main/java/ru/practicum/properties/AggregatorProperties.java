package ru.practicum.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(prefix = "my.area.guide")
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AggregatorProperties {

    Kafka kafka = new Kafka();
    Aggregator aggregator = new Aggregator();

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Kafka {
        private String userActionTopic = "user-actions";
        private String eventsSimilarityTopic = "events-similarity";
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Aggregator {
        private final Weights weights = new Weights();
        private String minimumSumAlgorithm = "optimized";
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Weights {
        private String like = "0.9";           // Вес действия "лайк"
        private String register = "0.7";       // Вес действия "регистрация"
        private String view = "0.3";           // Вес действия "просмотр"

        public BigDecimal calculateWeight(UserActionAvro action) {
            return switch (action.getActionType()) {
                case LIKE -> BigDecimal.valueOf(Double.parseDouble(this.like)); // Значение лайка
                case REGISTER -> BigDecimal.valueOf(Double.parseDouble(this.register)); // Значение регистрации
                default -> BigDecimal.valueOf(Double.parseDouble(this.view)); // Значение просмотра по умолчанию
            };
        }
    }

//    @Data
//    @EqualsAndHashCode(callSuper = false)
//    public static class Weights {
//        private String like = "0.9";
//        private String register = "0.7";
//        private String view = "0.3";
//
//        public BigDecimal ofUserAction(UserActionAvro action) {
//            return switch (action.getActionType()) {
//                case LIKE -> new BigDecimal(this.like);
//                case REGISTER -> new BigDecimal(this.register);
//                default -> new BigDecimal(this.view);
//            };
//        }
//    }
}

//package ru.practicum.properties;
//
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.experimental.FieldDefaults;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//import ru.practicum.ewm.stats.avro.UserActionAvro;
//
//import java.math.BigDecimal;
//
//@Getter
//@Setter
//@ConfigurationProperties("my-area-guide")
//@Component
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class AggregatorProperties {
//
//    Kafka kafka = new Kafka();
//    Aggregator aggregator = new Aggregator();
//
//    @Getter
//    @Setter
//    public static class Kafka {
//        private String userActionTopic = "user-actions";
//        private String eventsSimilarityTopic = "events-similarity";
//    }
//
//    @Getter
//    @Setter
//    public static class Aggregator {
//        private final Weights weights = new Weights();
//        private String minimumSumAlgorithm = "optimized";
//    }
//
//    @Getter
//    @Setter
//    public static class Weights {
//        private String like = "0.9";
//        private String register = "0.7";
//        private String view = "0.3";
//
//        public BigDecimal ofUserAction(UserActionAvro userActionAvro) {
//            return switch (userActionAvro.getActionType()) {
//                case LIKE -> new BigDecimal(like);
//                case REGISTER -> new BigDecimal(register);
//                default -> new BigDecimal(view);
//            };
//        }
//    }
//}