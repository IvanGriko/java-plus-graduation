package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.user.dto.UserDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentShortDto {

    Long id;

    String text;

    UserDto author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String createTime;

}