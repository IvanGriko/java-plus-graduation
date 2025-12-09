package ru.practicum.event.dal;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {

    Float lat;

    Float lon;

}
