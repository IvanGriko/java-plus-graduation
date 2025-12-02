package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentCreateDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentPrivateService;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController {

    private final CommentPrivateService service;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid CommentCreateDto commentCreateDto
    ) {
        log.info("Создание комментария пользователя с ID {} к событию с ID {}", userId, eventId);
        CommentDto createdComment = service.createComment(userId, eventId, commentCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @DeleteMapping("/users/{userId}/comments/{comId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long comId
    ) {
        log.info("Удаление комментария с ID {} пользователем с ID {}", comId, userId);
        service.deleteComment(userId, comId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/comments/{comId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long comId,
            @RequestBody @Valid CommentCreateDto commentCreateDto
    ) {
        log.info("Обновление комментария с ID {} пользователем с ID {}", comId, userId);
        CommentDto updatedComment = service.patchComment(userId, comId, commentCreateDto);
        return ResponseEntity.ok(updatedComment);
    }
}
