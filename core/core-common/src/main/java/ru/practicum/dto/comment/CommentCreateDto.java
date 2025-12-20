package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {

    @NotBlank(message = "Текст комментария обязателен")
    @Size(
            min = 1,
            max = 1000,
            message = "Длина текста комментария должна быть от 1 до 1000 символов"
    )
    private String text;
}