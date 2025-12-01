package ru.practicum.comment.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentAdminService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class CommentAdminController {

    private final CommentAdminService service;

    @GetMapping("/comments/search")
    public ResponseEntity<List<CommentDto>> searchComments(
            @RequestParam @NotBlank String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Поиск комментариев по тексту: {}", text);
        List<CommentDto> comments = service.search(text, from, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentDto>> getUserComments(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получение комментариев пользователя с ID={}", userId);
        List<CommentDto> comments = service.findAllByUserId(userId, from, size);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/{comId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long comId
    ) {
        log.info("Удаление комментария с ID={}", comId);
        service.delete(comId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/comments/{comId}/approve")
    public ResponseEntity<CommentDto> approveComment(
            @PathVariable @Positive Long comId
    ) {
        log.info("Одобрение комментария с ID={}", comId);
        CommentDto approvedComment = service.approveComment(comId);
        return ResponseEntity.ok(approvedComment);
    }

    @PatchMapping("/comments/{comId}/reject")
    public ResponseEntity<CommentDto> rejectComment(
            @PathVariable @Positive Long comId
    ) {
        log.info("Отклонение комментария с ID={}", comId);
        CommentDto rejectedComment = service.rejectComment(comId);
        return ResponseEntity.ok(rejectedComment);
    }
}
