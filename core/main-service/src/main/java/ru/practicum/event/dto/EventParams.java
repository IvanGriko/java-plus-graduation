package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventParams {

    String text;

    List<Long> categories;

    Boolean paid;

    LocalDateTime rangeStart;

    LocalDateTime rangeEnd;

    Boolean onlyAvailable;

    EventSort eventSort;

    Long from;

    Long size;

}
