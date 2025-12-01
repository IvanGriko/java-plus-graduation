package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.AtLeastOneNotNull;
import ru.practicum.validation.NotBlankButNullAllowed;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AtLeastOneNotNull(fields = {"events", "pinned", "title"},
        message = "Хотя бы одно из полей 'events', 'pinned' или 'title' должен быть заполнен")
public class UpdateCompilationDto {

    @Builder.Default
    Set<Long> events = new HashSet<>();

    Boolean pinned;

    @NotBlankButNullAllowed
    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов")
    String title;

}
