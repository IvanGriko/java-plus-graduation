package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventInteractionDto {

    Long id;
    Long initiatorId;
    Long categoryId;
    String title;
    String annotation;
    String description;
    State state;
    LocationDto location;
    Long participantLimit;
    Boolean requestModeration;
    Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    public static EventInteractionDto withOnlyId(Long id) {
        return EventInteractionDto.builder()
                .id(id)
                .build();
    }
}