package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    User requester;

    @ManyToOne
    @JoinColumn(name = "event_id")
    Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    ParticipationRequestStatus status;

    @Column(name = "created_at")
    LocalDateTime created;

}
