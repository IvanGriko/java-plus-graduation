package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentPublicApi;
import ru.practicum.comment.service.CommentPublicService;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentPublicController implements CommentPublicApi {

    private final CommentPublicService commentPublicService;

    @Override
    public CommentDto getById(Long comId) {
        log.info("Запрашивается полный комментарий с ID {}", comId);
        return commentPublicService.getComment(comId);
    }

    @Override
    public Collection<CommentShortDto> getByEventId(Long eventId, int from, int size) {
        log.info("Запрашиваются короткие комментарии для события с ID {}: начиная с {} элемента, размер выборки {}", eventId, from, size);
        return commentPublicService.getCommentsByEvent(eventId, from, size);
    }

    @Override
    public CommentDto getByEventAndCommentId(Long eventId, Long commentId) {
        log.info("Запрашивается полный комментарий с ID {} для события с ID {}", commentId, eventId);
        return commentPublicService.getCommentByEventAndCommentId(eventId, commentId);
    }

}