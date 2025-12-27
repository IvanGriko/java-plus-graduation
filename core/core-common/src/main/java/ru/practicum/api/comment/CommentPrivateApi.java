package ru.practicum.api.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;

public interface CommentPrivateApi {

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto create(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId,
            @RequestBody
            @Valid CommentCreateDto commentCreateDto
    );

    @DeleteMapping("/users/{userId}/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String delete(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId
    );

    @PatchMapping("/users/{userId}/comments/{comId}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto patch(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId,
            @RequestBody
            @Valid CommentCreateDto commentCreateDto
    );
}