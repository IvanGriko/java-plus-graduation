package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentAdminServiceImpl implements CommentAdminService {

    TransactionTemplate transactionTemplate;
    CommentRepository commentRepository;
    UserClientHelper userClientHelper;
    EventClientHelper eventClientHelper;

    @Override
    @Transactional
    public String delete(Long comId) {
        log.info("Начинается удаление комментария с ID {}", comId);
        if (!commentRepository.existsById(comId)) {
            log.warn("Комментарий с ID {} не найден.", comId);
            throw new NotFoundException("Комментарий с ID " + comId + " не найден.");
        }
        commentRepository.deleteById(comId);
        log.info("Комментарий с ID {} успешно удалён.", comId);
        return "Комментарий с ID " + comId + " успешно удалён.";
    }

    @Override
    public List<CommentDto> search(String text, int from, int size) {
        log.info("Начинается поиск комментариев по тексту \"{}\"", text);
        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size);
            return commentRepository.findByText(text, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) {
            log.info("Комментариев по тексту \"{}\" не найдено.", text);
            return List.of();
        }
        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap = userClientHelper.fetchUserDtoMapByUserIdList(userIds);
        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap = eventClientHelper.fetchEventCommentMapByIds(eventIds);
        List<CommentDto> result = comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userMap.get(c.getAuthorId()),
                        eventMap.get(c.getEventId())
                )).toList();
        log.info("Поиск комментариев по тексту \"{}\" завершился успешно. Найдено {} комментариев.", text, result.size());
        return result;
    }

    @Override
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        log.info("Начинается поиск комментариев пользователя с ID {}", userId);
        UserDto userDto = userClientHelper.fetchUserDtoByUserId(userId);
        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size);
            return commentRepository.findAllByAuthorId(userId, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) {
            log.info("Комментариев пользователя с ID {} не найдено.", userId);
            return List.of();
        }
        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
        Map<Long, EventCommentDto> eventMap = eventClientHelper.fetchEventCommentMapByIds(eventIds);
        List<CommentDto> result = comments.stream()
                .map(c -> CommentMapper.toCommentDto(
                        c,
                        userDto,
                        eventMap.get(c.getEventId())
                )).toList();
        log.info("Поиск комментариев пользователя с ID {} завершился успешно. Найдено {} комментариев.", userId, result.size());
        return result;
    }

    @Override
    public CommentDto approveComment(Long comId) {
        log.info("Начинается утверждение комментария с ID {}", comId);
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден."));
            commentEntity.setApproved(true);
            return commentRepository.save(commentEntity);
        });
        log.info("Комментарий с ID {} успешно утверждён.", comId);
        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    public CommentDto rejectComment(Long comId) {
        log.info("Начинается отклонение комментария с ID {}", comId);
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден."));
            commentEntity.setApproved(false);
            return commentRepository.save(commentEntity);
        });
        log.info("Комментарий с ID {} успешно отклонён.", comId);
        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}

//package ru.practicum.comment.service;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionTemplate;
//import ru.practicum.client.EventClientHelper;
//import ru.practicum.client.UserClientHelper;
//import ru.practicum.comment.dal.Comment;
//import ru.practicum.comment.repository.CommentRepository;
//import ru.practicum.dto.comment.CommentDto;
//import ru.practicum.dto.event.EventCommentDto;
//import ru.practicum.dto.user.UserDto;
//import ru.practicum.exception.NotFoundException;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class CommentAdminServiceImpl implements CommentAdminService {
//
//    TransactionTemplate transactionTemplate;
//    CommentRepository commentRepository;
//    UserClientHelper userClientHelper;
//    EventClientHelper eventClientHelper;
//
//    @Override
//    @Transactional
//    public String delete(Long comId) {
//        if (!commentRepository.existsById(comId)) {
//            log.warn("Комментарий с ID {} не найден.", comId);
//            throw new NotFoundException("Комментарий с ID " + comId + " не найден.");
//        }
//        commentRepository.deleteById(comId);
//        log.info("Комментарий с ID {} успешно удалён.", comId);
//        return "Комментарий с ID " + comId + " успешно удалён.";
//    }
//
//    @Override
//    public List<CommentDto> search(String text, int from, int size) {
//        log.info("Начинается поиск комментариев по тексту \"{}\"", text);
//        List<Comment> comments = transactionTemplate.execute(status -> {
//            Pageable pageable = PageRequest.of(from / size, size);
//            return commentRepository.findByText(text, pageable).getContent();
//        });
//        if (comments == null || comments.isEmpty()) {
//            log.info("Комментариев по тексту \"{}\" не найдено.", text);
//            return List.of();
//        }
//        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
//        Map<Long, UserDto> userMap = userClientHelper.fetchUserDtoMapByUserIdList(userIds);
//        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
//        Map<Long, EventCommentDto> eventMap = eventClientHelper.fetchEventCommentMapByIds(eventIds);
//        List<CommentDto> result = comments.stream()
//                .map(c -> CommentMapper.toCommentDto(
//                        c,
//                        userMap.get(c.getAuthorId()),
//                        eventMap.get(c.getEventId())
//                )).toList();
//        log.info("Поиск комментариев по тексту \"{}\" завершился успешно. Найдено {} комментариев.", text, result.size());
//        return result;
//    }
//
//    @Override
//    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
//        log.info("Начинается поиск комментариев пользователя с ID {}", userId);
//        UserDto userDto = userClientHelper.fetchUserDtoByUserId(userId);
//        List<Comment> comments = transactionTemplate.execute(status -> {
//            Pageable pageable = PageRequest.of(from / size, size);
//            return commentRepository.findAllByAuthorId(userId, pageable).getContent();
//        });
//        if (comments == null || comments.isEmpty()) {
//            log.info("Комментариев пользователя с ID {} не найдено.", userId);
//            return List.of();
//        }
//        Set<Long> eventIds = comments.stream().map(Comment::getEventId).collect(Collectors.toSet());
//        Map<Long, EventCommentDto> eventMap = eventClientHelper.fetchEventCommentMapByIds(eventIds);
//        List<CommentDto> result = comments.stream()
//                .map(c -> CommentMapper.toCommentDto(
//                        c,
//                        userDto,
//                        eventMap.get(c.getEventId())
//                )).toList();
//        log.info("Поиск комментариев пользователя с ID {} завершился успешно. Найдено {} комментариев.", userId, result.size());
//        return result;
//    }
//
//    @Override
//    public CommentDto approveComment(Long comId) {
//        log.info("Утверждение комментария с ID {}", comId);
//        Comment comment = transactionTemplate.execute(status -> {
//            Comment commentEntity = commentRepository.findById(comId)
//                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден."));
//            commentEntity.setApproved(true);
//            return commentRepository.save(commentEntity);
//        });
//        log.info("Комментарий с ID {} утверждён.", comId);
//        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
//        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
//        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
//    }
//
//    @Override
//    public CommentDto rejectComment(Long comId) {
//        log.info("Отклонение комментария с ID {}", comId);
//        Comment comment = transactionTemplate.execute(status -> {
//            Comment commentEntity = commentRepository.findById(comId)
//                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден."));
//            commentEntity.setApproved(false);
//            return commentRepository.save(commentEntity);
//        });
//        log.info("Комментарий с ID {} отклонён.", comId);
//        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
//        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
//        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
//    }
//
//}