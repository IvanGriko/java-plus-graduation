package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentPrivateApi;
import ru.practicum.comment.service.CommentPrivateService;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController implements CommentPrivateApi {

    private final CommentPrivateService commentPrivateService;

    @Override
    public CommentDto create(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        log.info("Создается комментарий пользователя с ID {} к событию с ID {}", userId, eventId);
        return commentPrivateService.createComment(userId, eventId, commentCreateDto);
    }

    @Override
    public String delete(Long userId, Long comId) {
        log.info("Пользователь с ID {} удаляет комментарий с ID {}", userId, comId);
        return commentPrivateService.deleteComment(userId, comId);
    }

    @Override
    public CommentDto patch(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        log.info("Редактируется комментарий с ID {} пользователем с ID {}", comId, userId);
        return commentPrivateService.patchComment(userId, comId, commentCreateDto);
    }
}