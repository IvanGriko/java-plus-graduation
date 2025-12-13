package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {

    @Builder.Default
    Set<Long> events = new HashSet<>();

    @Builder.Default
    Boolean pinned = false;

    @NotBlank(message = "Заголовок подборки обязателен")
    @Size(min = 1, max = 50, message = "Длина заголовка должна быть от 1 до 50 символов")
    String title;

}