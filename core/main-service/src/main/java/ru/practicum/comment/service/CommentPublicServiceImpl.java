package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.util.Util.createPageRequestAsc;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentPublicServiceImpl implements CommentPublicService {

    CommentRepository repository;
    EventRepository eventRepository;

    @Override
    public CommentDto getComment(Long comId) {
        log.info("Получение комментария с ID={}", comId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Комментарий с ID={} не найден", comId);
                    return new NotFoundException("Комментарий не найден");
                });
        if (!comment.isApproved()) {
            log.warn("Комментарий с ID={} не одобрен", comId);
            throw new ForbiddenException("Комментарий не одобрен");
        }
        log.info("Комментарий с ID={} успешно получен", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        log.info("Получение комментариев для события с ID={}", eventId);
        if (!eventRepository.existsById(eventId)) {
            log.error("Событие с ID={} не найдено", eventId);
            throw new NotFoundException("Событие не найдено");
        }
        Pageable pageable = createPageRequestAsc("createTime", from, size);
        Page<Comment> commentsPage = repository.findAllByEventId(eventId, pageable);
        List<Comment> comments = commentsPage.getContent();
        List<Comment> approvedComments = comments.stream()
                .filter(Comment::isApproved)
                .collect(Collectors.toList());
        log.info("Получено {} одобренных комментариев", approvedComments.size());
        return CommentMapper.toListCommentShortDto(approvedComments);
    }

    @Override
    public CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId) {
        log.info("Получение комментария с ID={} для события с ID={}", commentId, eventId);
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Комментарий с ID={} не найден", commentId);
                    return new NotFoundException("Комментарий не найден");
                });
        if (!comment.getEvent().getId().equals(eventId)) {
            log.error("Комментарий с ID={} не принадлежит событию с ID={}", commentId, eventId);
            throw new NotFoundException("Комментарий не найден для указанного события");
        }
        if (!comment.isApproved()) {
            log.warn("Комментарий с ID={} не одобрен", commentId);
            throw new ForbiddenException("Комментарий не одобрен");
        }
        log.info("Комментарий с ID={} успешно получен", commentId);
        return CommentMapper.toCommentDto(comment);
    }
}

//@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class CommentPublicServiceImpl implements CommentPublicService {
//
//    CommentRepository repository;
//    EventRepository eventRepository;
//
//    @Override
//    public CommentDto getComment(Long comId) {
//        log.info("getComment - invoked");
//        Comment comment = repository.findById(comId)
//                .orElseThrow(() -> {
//                    log.error("Comment with id = {} - not exist", comId);
//                    return new NotFoundException("Comment not found");
//                });
//        if (!comment.isApproved()) {
//            log.warn("Comment with id = {} is not approved", comId);
//            throw new ForbiddenException("Comment is not approved");
//        }
//        log.info("Result: comment with id= {}", comId);
//        return CommentMapper.toCommentDto(comment);
//    }
//
//    @Override
//    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
//        log.info("getCommentsByEvent - invoked");
//        if (!eventRepository.existsById(eventId)) {
//            log.error("Event with id = {} - not exist", eventId);
//            throw new NotFoundException("Event not found");
//        }
//        Pageable pageable = createPageRequestAsc("createTime", from, size);
//        Page<Comment> commentsPage = repository.findAllByEventId(eventId, pageable);
//        List<Comment> comments = commentsPage.getContent();
//        List<Comment> approvedComments = comments.stream()
//                .filter(Comment::isApproved)
//                .collect(Collectors.toList());
//        log.info("Result : list of approved comments size = {}", approvedComments.size());
//        return CommentMapper.toListCommentShortDto(approvedComments);
//    }
//
//    @Override
//    public CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId) {
//        log.info("getCommentByEventAndCommentId - invoked");
//        Comment comment = repository.findById(commentId)
//                .orElseThrow(() -> {
//                    log.error("Comment with id = {} does not exist", commentId);
//                    return new NotFoundException("Comment not found");
//                });
//        if (!comment.getEvent().getId().equals(eventId)) {
//            log.error("Comment with id = {} does not belong to event with id = {}", commentId, eventId);
//            throw new NotFoundException("Comment not found for the specified event");
//        }
//        if (!comment.isApproved()) {
//            log.warn("Comment with id = {} is not approved", commentId);
//            throw new ForbiddenException("Comment is not approved");
//        }
//        log.info("Result: comment with eventId= {} and commentId= {}", eventId, commentId);
//        return CommentMapper.toCommentDto(comment);
//    }
//}