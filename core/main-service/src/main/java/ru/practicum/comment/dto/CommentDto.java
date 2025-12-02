package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.dto.EventCommentDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Long id;

    String text;

    UserDto author;

    EventCommentDto event;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime patchTime;

    Boolean approved;

}
