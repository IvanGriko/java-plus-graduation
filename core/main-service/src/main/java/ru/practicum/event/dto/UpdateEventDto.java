package ru.practicum.event.dto;

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
        "participantLimit", "requestModeration", "stateAction", "eventDate"})
public class UpdateEventDto {

    @Positive
    Long category;

    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    String description;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero
    Long participantLimit;

    Boolean requestModeration;

    StateAction stateAction;

    @Future(message = "Event should be in future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

}
