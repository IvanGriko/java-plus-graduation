package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventHitDto {

    @NotNull(message = "Поле 'app' обязательно для заполнения.")
    String app;

    @NotNull(message = "Поле 'uri' должно указывать ресурс, с которым связано событие.")
    String uri;

    @NotNull(message = "Поле 'ip' обязательно для идентификации пользователя.")
    String ip;

    @NotNull(message = "Поле 'timestamp' должно точно указывать время произошедшего события.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
}