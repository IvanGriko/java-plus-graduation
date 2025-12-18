package ru.practicum.request.dal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.request.ParticipationRequestStatus;

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
@Table(name = "requests", indexes = {
        @Index(name = "idx_requests_requester_id", columnList = "requester_id"),
        @Index(name = "idx_requests_event_id", columnList = "event_id")
})

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    Long id;

    @Column(name = "requester_id", nullable = false)
    Long requesterId;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    ParticipationRequestStatus status;

    @Column(name = "created_at", nullable = false)
    LocalDateTime created;

}