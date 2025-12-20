package ru.practicum.api.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;

import java.util.Collection;

public interface CommentAdminApi {

    @GetMapping("/admin/comments/search")
    @ResponseStatus(HttpStatus.OK)
    Collection<CommentDto> search(
            @RequestParam
            @NotBlank(message = "Текст поиска не может быть пустым.") String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/admin/users/{userId}/comments")
    @ResponseStatus(HttpStatus.OK)
    Collection<CommentDto> get(
            @PathVariable
            @Positive(message = "Идентификатор пользователя должен быть положительным числом.") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @DeleteMapping("/admin/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String delete(
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId
    );

    @PatchMapping("/admin/comments/{comId}/approve")
    @ResponseStatus(HttpStatus.OK)
    CommentDto approveComment(
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId
    );

    @PatchMapping("/admin/comments/{comId}/reject")
    @ResponseStatus(HttpStatus.OK)
    CommentDto rejectComment(
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId
    );
}