package ru.practicum.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventAdminParams {

    List<Long> users;

    List<State> states;

    List<Long> categories;

    LocalDateTime rangeStart;

    LocalDateTime rangeEnd;

    Integer from;

    Integer size;

}
