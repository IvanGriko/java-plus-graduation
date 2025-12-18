package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentAdminApi;
import ru.practicum.comment.service.CommentAdminService;
import ru.practicum.dto.comment.CommentDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentAdminController implements CommentAdminApi {

    private final CommentAdminService commentAdminService;

    @Override
    public Collection<CommentDto> search(String text, int from, int size) {
        log.info("Выполняется поиск комментариев по запросу '{}', начиная с {} элемента, размер выборки {}", text, from, size);
        return commentAdminService.search(text, from, size);
    }

    @Override
    public Collection<CommentDto> get(Long userId, int from, int size) {
        log.info("Получение списка комментариев пользователя с ID {}, начиная с {} элемента, размер выборки {}", userId, from, size);
        return commentAdminService.findAllByUserId(userId, from, size);
    }

    @Override
    public String delete(Long comId) {
        log.info("Удаляется комментарий с ID {}", comId);
        return commentAdminService.delete(comId);
    }

    @Override
    public CommentDto approveComment(Long comId) {
        log.info("Одобряется комментарий с ID {}", comId);
        return commentAdminService.approveComment(comId);
    }

    @Override
    public CommentDto rejectComment(Long comId) {
        log.info("Отвергается комментарий с ID {}", comId);
        return commentAdminService.rejectComment(comId);
    }

}