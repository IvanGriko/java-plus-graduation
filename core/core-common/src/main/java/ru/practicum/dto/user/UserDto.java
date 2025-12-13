package ru.practicum.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    String email;

    String name;

    public static UserDto withOnlyId(Long id) {
        return UserDto.builder()
                .id(id)
                .build();
    }

}