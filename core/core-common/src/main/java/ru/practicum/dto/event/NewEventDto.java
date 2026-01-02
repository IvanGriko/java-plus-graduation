package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @NotNull(message = "Категория события обязательна")
    @Positive(message = "ID категории должен быть положительным числом")
    Long category;

    @NotBlank(message = "Название события обязательно")
    @Size(
            min = 3,
            max = 120,
            message = "Название события должно быть длиной от 3 до 120 символов"
    )
    String title;

    @NotBlank(message = "Краткое описание события обязательно")
    @Size(
            min = 20,
            max = 2000,
            message = "Краткое описание должно быть длиной от 20 до 2000 символов"
    )
    String annotation;

    @NotBlank(message = "Полное описание события обязательно")
    @Size(
            min = 20,
            max = 7000,
            message = "Полное описание должно быть длиной от 20 до 7000 символов"
    )
    String description;

    LocationDto location;
    Boolean requestModeration = true;
    Boolean paid = false;

    @PositiveOrZero(message = "Максимальное количество участников должно быть неотрицательным числом")
    Long participantLimit = 0L;

    @NotNull(message = "Дата события обязательна")
    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
}