package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;

import java.util.List;

public interface CommentPublicService {

    public CommentDto getComment(Long comId);

    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size);

    public CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId);
}
