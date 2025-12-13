package ru.practicum.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventCommentDto {

    Long id;

    String title;

    State state;

    public static EventCommentDto withOnlyId(Long id) {
        return EventCommentDto.builder()
                .id(id)
                .build();
    }

}
