package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.comment.CommentCreateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentPrivateServiceImpl implements CommentPrivateService {

    TransactionTemplate transactionTemplate;
    CommentRepository commentRepository;
    UserClientHelper userClientHelper;
    EventClientHelper eventClientHelper;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        UserDto userDto = userClientHelper.fetchUserDtoByUserIdOrFail(userId);
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentByIdOrFail(eventId);
        if (!Objects.equals(eventCommentDto.getState(), State.PUBLISHED)) {
            log.warn("Не удается прокомментировать неопубликованное событие с ID {}", eventId);
            throw new ConflictException("Нельзя комментировать неопубликованное событие с ID " + eventId);
        }
        return transactionTemplate.execute(status -> {
            Comment comment = Comment.builder()
                    .text(commentCreateDto.getText())
                    .authorId(userId)
                    .eventId(eventId)
                    .approved(true)
                    .createTime(LocalDateTime.now())
                    .build();
            commentRepository.save(comment);
            log.info("Комментарий с ID {} успешно создан", comment.getId());
            return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
        });
    }

    @Override
    @Transactional
    public String deleteComment(Long userId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден"));
        if (!Objects.equals(comment.getAuthorId(), userId)) {
            log.warn("Пользователь с ID {} пытается удалить чужой комментарий с ID {}", userId, comId);
            throw new ConflictException("Пользователь с ID " + userId + " не имеет права удалять комментарий с ID " + comId);
        }
        commentRepository.deleteById(comId);
        log.info("Комментарий с ID {} успешно удалён", comId);
        return "Комментарий с ID " + comId + " успешно удалён";
    }

    @Override
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        Comment comment = transactionTemplate.execute(status -> {
            Comment commentEntity = commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден"));
            if (!Objects.equals(commentEntity.getAuthorId(), userId)) {
                log.warn("Пользователь с ID {} пытается изменить чужой комментарий с ID {}", userId, comId);
                throw new ConflictException("Пользователь с ID " + userId + " не имеет права редактировать комментарий с ID " + comId);
            }
            commentEntity.setText(commentCreateDto.getText());
            commentEntity.setPatchTime(LocalDateTime.now());
            return commentRepository.save(commentEntity);
        });
        UserDto userDto = userClientHelper.fetchUserDtoByUserIdOrFail(userId);
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentByIdOrFail(comment.getEventId());
        log.info("Комментарий с ID {} успешно обновлён", comId);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

}
