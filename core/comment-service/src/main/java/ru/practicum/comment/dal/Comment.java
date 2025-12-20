package ru.practicum.comment.dal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_event_id", columnList = "event_id"),
        @Index(name = "idx_comments_textual_content", columnList = "textual_content")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "textual_content", length = 1000, nullable = false)
    String text;

    @Column(name = "author_id", nullable = false)
    Long authorId;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "create_time", nullable = false)
    LocalDateTime createTime;

    @Column(name = "patch_time")
    LocalDateTime patchTime;

    @Column(name = "approved", nullable = false)
    Boolean approved;
}