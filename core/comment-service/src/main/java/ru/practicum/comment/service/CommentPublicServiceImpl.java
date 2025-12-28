package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.EventClientHelper;
import ru.practicum.client.UserClientHelper;
import ru.practicum.comment.dal.Comment;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.event.EventCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentPublicServiceImpl implements CommentPublicService {

    TransactionTemplate transactionTemplate;
    CommentRepository commentRepository;
    UserClientHelper userClientHelper;
    EventClientHelper eventClientHelper;

    @Override
    public CommentDto getComment(Long comId) {
        log.info("Начало получения комментария с ID {}", comId);
        Comment comment = transactionTemplate.execute(status -> {
            return commentRepository.findById(comId)
                    .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден"));
        });
        if (!Objects.equals(comment.getApproved(), true)) {
            log.warn("Комментарий с ID {} ожидает одобрения", comId);
            throw new ForbiddenException("Комментарий с ID " + comId + " ожидает одобрения");
        }
        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
        log.info("Комментарий с ID {} успешно получен", comId);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        log.info("Начало получения комментариев для события с ID {}", eventId);
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(eventId);
        List<Comment> comments = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("createTime").ascending());
            return commentRepository.findAllByEventIdAndApproved(eventId, true, pageable).getContent();
        });
        if (comments == null || comments.isEmpty()) return List.of();
        Set<Long> userIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userMap = userClientHelper.fetchUserDtoMapByUserIdList(userIds);
        List<CommentShortDto> result = comments.stream()
                .map(c -> CommentMapper.toCommentShortDto(c, userMap.get(c.getAuthorId())))
                .toList();
        log.info("Получены комментарии для события с ID {}: {} штук", eventId, result.size());
        return result;
    }

    @Override
    public CommentDto getCommentByEventAndCommentId(Long eventId, Long comId) {
        log.info("Начало получения комментария с ID {} для события с ID {}", comId, eventId);
        Comment comment = transactionTemplate.execute(status -> commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + comId + " не найден")));
        if (!Objects.equals(comment.getEventId(), eventId)) {
            log.warn("Комментарий с ID {} не относится к событию с ID {}", comId, eventId);
            throw new NotFoundException("Комментарий с ID " + comId + " не относится к событию с ID " + eventId);
        }
        if (!Objects.equals(comment.getApproved(), true)) {
            log.warn("Комментарий с ID {} ожидает одобрения", comId);
            throw new ForbiddenException("Комментарий с ID " + comId + " ожидает одобрения");
        }
        UserDto userDto = userClientHelper.fetchUserDtoByUserId(comment.getAuthorId());
        EventCommentDto eventCommentDto = eventClientHelper.fetchEventCommentById(comment.getEventId());
        log.info("Комментарий с ID {} успешно получен", comId);
        return CommentMapper.toCommentDto(comment, userDto, eventCommentDto);
    }
}