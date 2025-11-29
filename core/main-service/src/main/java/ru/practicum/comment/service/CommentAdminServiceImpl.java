package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentAdminServiceImpl implements CommentAdminService {

    CommentRepository repository;
    UserRepository userRepository;

    @Override
    public void delete(Long comId) {
        log.info("Администратор удаляет комментарий с ID={}", comId);
        if (!repository.existsById(comId)) {
            log.error("Комментарий с ID={} не найден", comId);
            throw new NotFoundException("Комментарий не найден");
        }
        repository.deleteById(comId);
        log.info("Комментарий с ID={} успешно удален", comId);
    }

    @Override
    public List<CommentDto> search(String text, int from, int size) {
        log.info("Администратор ищет комментарии по тексту: {}", text);
        Pageable pageable = createPageRequest(from, size);
        Page<Comment> page = repository.findAllByText(text, pageable);
        List<Comment> comments = page.getContent();
        log.info("Найдено {} комментариев", comments.size());
        return CommentMapper.toListCommentDto(comments);
    }

    @Override
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        log.info("Администратор получает комментарии пользователя с ID={}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с ID={} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }
        Pageable pageable = createPageRequest(from, size);
        Page<Comment> page = repository.findAllByAuthorId(userId, pageable);
        List<Comment> comments = page.getContent();
        log.info("Найдено {} комментариев пользователя", comments.size());
        return CommentMapper.toListCommentDto(comments);
    }

    @Override
    @Transactional
    public CommentDto approveComment(Long comId) {
        log.info("Администратор одобряет комментарий с ID={}", comId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        comment.setApproved(true);
        repository.save(comment);
        log.info("Комментарий с ID={} одобрен", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long comId) {
        log.info("Администратор отклоняет комментарий с ID={}", comId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        comment.setApproved(false);
        repository.save(comment);
        log.info("Комментарий с ID={} отклонен", comId);
        return CommentMapper.toCommentDto(comment);
    }

    // Вспомогательный метод для расчета страничного запроса
    private Pageable createPageRequest(int from, int size) {
        return PageRequest.of(from / size, size);
    }
}

//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class CommentAdminServiceImpl implements CommentAdminService {
//
//    CommentRepository repository;
//    UserRepository userRepository;
//
//    @Override
//    public void delete(Long comId) {
//        log.info("admin delete - invoked");
//        if (!repository.existsById(comId)) {
//            log.error("User with id = {} not exist", comId);
//            throw new NotFoundException("Comment not found");
//        }
//        log.info("Result: comment with id = {} deleted", comId);
//        repository.deleteById(comId);
//    }
//
//    @Override
//    public List<CommentDto> search(String text, int from, int size) {
//        log.info("admin search - invoked");
//        Pageable pageable = PageRequest.of(from / size, size);
//        Page<Comment> page = repository.findAllByText(text, pageable);
//        List<Comment> list = page.getContent();
//        log.info("Result: list of comments size = {} ", list.size());
//        return CommentMapper.toListCommentDto(list);
//    }
//
//    @Override
//    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
//        log.info("admin findAllByUserId - invoked");
//        if (!userRepository.existsById(userId)) {
//            log.error("User with id = {} not exist", userId);
//            throw new NotFoundException("User not found");
//        }
//        Pageable pageable = PageRequest.of(from / size, size);
//        Page<Comment> page = repository.findAllByAuthorId(userId, pageable);
//        List<Comment> list = page.getContent();
//        log.info("Result: list of comments size = {} ", list.size());
//        return CommentMapper.toListCommentDto(list);
//    }
//
//    @Override
//    public CommentDto approveComment(Long comId) {
//        log.info("approveComment - invoked");
//        Comment comment = repository.findById(comId)
//                .orElseThrow(() -> new NotFoundException("Comment not found"));
//        comment.setApproved(true);
//        repository.save(comment);
//        log.info("Result: comment with id = {} approved", comId);
//        return CommentMapper.toCommentDto(comment);
//    }
//
//    @Override
//    public CommentDto rejectComment(Long comId) {
//        log.info("rejectComment - invoked");
//        Comment comment = repository.findById(comId).orElseThrow(() -> new NotFoundException("Comment not found"));
//        comment.setApproved(false);
//        repository.save(comment);
//        log.info("Result: comment with id = {} rejected", comId);
//        return CommentMapper.toCommentDto(comment);
//    }
//}