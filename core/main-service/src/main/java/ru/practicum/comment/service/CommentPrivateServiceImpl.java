package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentCreateDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentPrivateServiceImpl implements CommentPrivateService {

    CommentRepository repository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto) {
        log.info("Создание комментария пользователем с ID={} к событию с ID={}", userId, eventId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным ID не зарегистрирован"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с указанным ID не найдено"));
        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Событие с ID={} не опубликовано", eventId);
            throw new ConflictException("Только опубликованное событие доступно для комментариев");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setApproved(true);
        comment.setCreateTime(LocalDateTime.now());
        log.info("Комментарий успешно создан");
        return CommentMapper.toCommentDto(repository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long comId) {
        log.info("Удаление комментария с ID={} пользователем с ID={}", comId, userId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий с указанным ID не найден"));
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Пользователь с ID={} пытается удалить чужой комментарий", userId);
            throw new ConflictException("Вы не можете удалить чужой комментарий");
        }
        log.info("Комментарий с ID={} успешно удален", comId);
        repository.deleteById(comId);
    }

    @Override
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        log.info("Редактирование комментария с ID={} пользователем с ID={}", comId, userId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий с указанным ID не найден"));
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Пользователь с ID={} пытается редактировать чужой комментарий", userId);
            throw new ConflictException("Вы не можете редактировать чужой комментарий");
        }
        comment.setText(commentCreateDto.getText());
        comment.setPatchTime(LocalDateTime.now()); // Используем текущее время без округления
        log.info("Комментарий с ID={} успешно обновлен", comId);
        return CommentMapper.toCommentDto(comment);
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class CommentPrivateServiceImpl implements CommentPrivateService {
//
//    CommentRepository repository;
//    UserRepository userRepository;
//    EventRepository eventRepository;
//
//    @Override
//    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto) {
//        log.info("createComment - invoked");
//        Comment comment = CommentMapper.toComment(commentDto);
//        User author = userRepository.findById(userId)
//                .orElseThrow(() -> {
//                    log.error("User with id = {} - not registered", userId);
//                    return new NotFoundException("Please register first then you can comment");
//                });
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> {
//                    log.error("Event with id = {} - not exist", eventId);
//                    return new NotFoundException("Event not found");
//                });
//        if (!event.getState().equals(State.PUBLISHED)) {
//            log.error("Event state = {} - should be PUBLISHED", event.getState());
//            throw new ConflictException("Event not published you cant comment it");
//        }
//        comment.setAuthor(author);
//        comment.setEvent(event);
//        comment.setApproved(true);   // по умолчанию комменты видны, но админ может удалить/вернуть
//        comment.setCreateTime(LocalDateTime.now().withNano(0));
//        log.info("Result: new comment created");
//        return CommentMapper.toCommentDto(repository.save(comment));
//    }
//
//    @Override
//    public void deleteComment(Long userId, Long comId) {
//        log.info("deleteComment - invoked");
//        Comment comment = repository.findById(comId)
//                .orElseThrow(() -> {
//                    log.error("Comment with id = {} - not exist", comId);
//                    return new NotFoundException("Comment not found");
//                });
//        if (!comment.getAuthor().getId().equals(userId)) {
//            log.error("Unauthorized access by user");
//            throw new ConflictException("you didn't write this comment and can't delete it");
//        }
//        log.info("Result: comment with id = {} - deleted", comId);
//        repository.deleteById(comId);
//    }
//
//    @Override
//    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
//        log.info("patchComment - invoked");
//        Comment comment = repository.findById(comId)
//                .orElseThrow(() -> {
//                    log.error("Comment with id = {} - not exist", comId);
//                    return new NotFoundException("Comment not found");
//                });
//        if (!comment.getAuthor().getId().equals(userId)) {
//            log.error("Unauthorized access by user");
//            throw new ConflictException("you didn't write this comment and can't patch it");
//        }
//        comment.setText(commentCreateDto.getText());
//        comment.setPatchTime(LocalDateTime.now().withNano(0));
//        log.info("Result: comment with id = {} - updated", comId);
//        return CommentMapper.toCommentDto(comment);
//    }
//}