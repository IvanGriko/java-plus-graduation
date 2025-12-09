package ru.practicum.category.dal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UniqueElements;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_cat_name", columnList = "cat_name")
})
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    Long id;

    @Size(min = 1, max = 50, message = "Название категории должно быть от 1 до 50 символов")
    @NotEmpty(message = "Название категории обязательно")
    @Column(name = "cat_name", length = 50, nullable = false, unique = true)
    String name;

}