package ru.practicum.compilation.dal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.event.dal.Event;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    Long id;

    @Column(name = "pinned", nullable = false)
    Boolean pinned;

    @Size(
            min = 1,
            max = 50,
            message = "Название должно быть от 1 до 50 символов"
    )
    @NotEmpty(message = "Название компиляции обязательно")
    @Column(name = "title", length = 50, nullable = false)
    String title;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilations_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "events_id", nullable = false))
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<Event> events;
}