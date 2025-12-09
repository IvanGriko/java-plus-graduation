package ru.practicum.event.dal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.category.dal.Category;
import ru.practicum.dto.event.State;

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
@Table(name = "events", indexes = {
        @Index(name = "idx_events_initiator_id", columnList = "initiator_id"),
        @Index(name = "idx_events_categories_id", columnList = "categories_id")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    Long id;

    @Column(name = "initiator_id", nullable = false)
    Long initiatorId;

    @ManyToOne
    @JoinColumn(name = "categories_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    Category category;

    @Column(name = "title", length = 120, nullable = false)
    String title;

    @Column(name = "annotation", length = 2000, nullable = false)
    String annotation;

    @Column(name = "description", length = 7000, nullable = false)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    State state;

    @Embedded
    Location location;

    @Column(name = "participant_limit", nullable = false)
    Long participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "paid", nullable = false)
    Boolean paid;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

}
