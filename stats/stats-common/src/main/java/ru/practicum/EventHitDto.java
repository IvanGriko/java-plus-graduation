package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventHitDto {

    @NotNull(message = "Field 'app' should not be null")
    String app;

    @NotNull(message = "Field 'uri' should not be null")
    String uri;

    @NotNull(message = "Field 'ip' should not be null")
    String ip;

    @NotNull(message = "Field 'timestamp' should not be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;

}
