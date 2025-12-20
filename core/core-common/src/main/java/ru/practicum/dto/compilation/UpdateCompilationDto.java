package ru.practicum.dto.compilation;

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
        message = "Необходимо заполнить хотя бы одно поле для обновления")
public class UpdateCompilationDto {

    @Builder.Default
    Set<Long> events = new HashSet<>();

    Boolean pinned;

    @NotBlankButNullAllowed(message = "Название не может быть пустым, если передано")
    @Size(
            min = 1,
            max = 50,
            message = "Длина названия должна быть от 1 до 50 символов"
    )
    String title;

}