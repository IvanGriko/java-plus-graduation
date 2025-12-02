package ru.practicum.jsontest;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.env.Environment;
import ru.practicum.dto.EventHitDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Выполнять только при запущенных Discovery и Config серверах")
@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class EventHitDtoJsonTest {

    @Autowired
    JacksonTester<EventHitDto> json;

    @Autowired
    Environment env;

    DateTimeFormatter formatter;

    @PostConstruct
    void setup() {
        String dateTimeFormat = env.getProperty("explore-with-me.datetime.format");
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Test
    void testBasicSerialization() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.JULY, 15, 14, 30, 45);
        EventHitDto eventHit = EventHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(timestamp)
                .build();

        String expectedJson = "{" +
                "\n\t\"app\": \"main-service\"," +
                "\n\t\"uri\": \"/events/1\"," +
                "\n\t\"ip\": \"192.168.1.1\"," +
                "\n\t\"timestamp\": \"%s\"" +
                "\n}".formatted(timestamp.format(formatter));

        assertThat(json.write(eventHit))
                .as("Проверка сериализации объекта EventHitDto")
                .isStrictlyEqualToJson(expectedJson);
    }

    @Test
    void testDeserializationWithValidJSON() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.DECEMBER, 25, 23, 59, 59);
        String content = "{" +
                "\n\t\"app\": \"main-service\"," +
                "\n\t\"uri\": \"/events/1\"," +
                "\n\t\"ip\": \"192.168.1.1\"," +
                "\n\t\"timestamp\": \"%s\"" +
                "\n}".formatted(timestamp.format(formatter));

        EventHitDto eventHit = json.parse(content).getObject();

        assertThat(eventHit.getApp())
                .as("Проверка атрибута 'app'")
                .isEqualTo("main-service");
        assertThat(eventHit.getUri())
                .as("Проверка атрибута 'uri'")
                .isEqualTo("/events/1");
        assertThat(eventHit.getIp())
                .as("Проверка атрибута 'ip'")
                .isEqualTo("192.168.1.1");
        assertThat(eventHit.getTimestamp())
                .as("Проверка атрибута 'timestamp'")
                .isEqualTo(timestamp);
    }

    @Test
    void testAlternativeDateTimeFormats() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.AUGUST, 20, 12, 0, 0);
        EventHitDto eventHit = EventHitDto.builder()
                .app("integration-test")
                .uri("/api/v1/events/123")
                .ip("172.16.0.1")
                .timestamp(timestamp)
                .build();

        assertThat(json.write(eventHit))
                .hasJsonPath("$.timestamp")
                .extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(formatter));
    }

    @Test
    void testDeserializationWithSpecialCharactersInURI() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0);
        String content = "{" +
                "\n\t\"app\": \"test-app\"," +
                "\n\t\"uri\": \"/events/search?query=test&eventSort=date\"," +
                "\n\t\"ip\": \"127.0.0.1\"," +
                "\n\t\"timestamp\": \"%s\"" +
                "\n}".formatted(timestamp.format(formatter));

        EventHitDto eventHit = json.parse(content).getObject();

        assertThat(eventHit.getApp())
                .as("Проверка атрибута 'app'")
                .isEqualTo("test-app");
        assertThat(eventHit.getUri())
                .as("Проверка атрибута 'uri'")
                .isEqualTo("/events/search?query=test&eventSort=date");
        assertThat(eventHit.getIp())
                .as("Проверка атрибута 'ip'")
                .isEqualTo("127.0.0.1");
        assertThat(eventHit.getTimestamp())
                .as("Проверка атрибута 'timestamp'")
                .isEqualTo(timestamp);
    }

    @Test
    void testTimestampDeserializationAccuracy() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.MARCH, 15, 16, 45, 22);
        String content = "{" +
                "\n\t\"app\": \"web-app\"," +
                "\n\t\"uri\": \"/\"," +
                "\n\t\"ip\": \"203.0.113.1\"," +
                "\n\t\"timestamp\": \"%s\"" +
                "\n}".formatted(timestamp.format(formatter));

        EventHitDto eventHit = json.parse(content).getObject();

        assertThat(eventHit.getTimestamp())
                .as("Проверка точного воспроизведения поля 'timestamp'")
                .isEqualTo(timestamp);
    }

    @Test
    void testCompleteSerializationCycle() throws Exception {
        EventHitDto original = EventHitDto.builder()
                .app("round-trip-test")
                .uri("/test/roundtrip")
                .ip("192.168.1.100")
                .timestamp(LocalDateTime.of(2024, Month.MAY, 10, 15, 30, 45))
                .build();

        String serialized = json.write(original).getJson();
        EventHitDto deserialized = json.parse(serialized).getObject();

        assertThat(deserialized)
                .as("Проверка целостности объекта после циклической операции")
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void testDeserializationEdgeCases() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, Month.JUNE, 30, 23, 59, 59);
        String content = "{" +
                "\n\t\"app\": \"edge-case-app\"," +
                "\n\t\"uri\": \"/special/path\"," +
                "\n\t\"ip\": \"192.168.1.255\"," +
                "\n\t\"timestamp\": \"%s\"" +
                "\n}".formatted(timestamp.format(formatter));

        EventHitDto eventHit = json.parse(content).getObject();

        assertThat(eventHit.getApp())
                .as("Проверка атрибута 'app' при экстремальном случае")
                .isEqualTo("edge-case-app");
        assertThat(eventHit.getUri())
                .as("Проверка атрибута 'uri' при экстремальном случае")
                .isEqualTo("/special/path");
        assertThat(eventHit.getIp())
                .as("Проверка атрибута 'ip' при экстремальном случае")
                .isEqualTo("192.168.1.255");
        assertThat(eventHit.getTimestamp())
                .as("Проверка атрибута 'timestamp' при экстремальном случае")
                .isEqualTo(timestamp);
    }
}