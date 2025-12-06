package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.AtLeastOneNotNull;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AtLeastOneNotNull(fields = {"category", "title", "annotation", "description", "location", "paid",
        "participantLimit", "requestModeration", "stateAction", "eventDate"},
        message = "Нужно заполнить хотя бы одно поле для обновления")
public class UpdateEventDto {

    @Positive(message = "Категория должна быть положительным числом")
    Long category;

    @Size(min = 3, max = 120, message = "Название должно быть длиной от 3 до 120 символов")
    String title;

    @Size(min = 20, max = 2000, message = "Краткое описание должно быть длиной от 20 до 2000 символов")
    String annotation;

    @Size(min = 20, max = 7000, message = "Полное описание должно быть длиной от 20 до 7000 символов")
    String description;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "Лимит участников должен быть неотрицательным числом")
    Long participantLimit;

    Boolean requestModeration;

    StateAction stateAction;

    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

}