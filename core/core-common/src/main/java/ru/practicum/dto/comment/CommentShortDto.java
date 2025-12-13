package ru.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.user.UserDto;

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