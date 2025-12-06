package ru.practicum.api.comment;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;

import java.util.Collection;

public interface CommentPublicApi {

    @GetMapping("/comments/{comId}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto getById(
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long comId
    );

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    Collection<CommentShortDto> getByEventId(
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    CommentDto getByEventAndCommentId(
            @PathVariable
            @Positive(message = "Идентификатор события должен быть положительным числом.") Long eventId,
            @PathVariable
            @Positive(message = "Идентификатор комментария должен быть положительным числом.") Long commentId
    );

}