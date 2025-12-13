package ru.practicum.user.dal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users", indexes = {@Index(name = "idx_users_email", columnList = "email")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    Long id;

    @Column(name = "email", length = 254, nullable = false, unique = true)
    String email;

    @Column(name = "name", length = 250, nullable = false)
    String name;

}
