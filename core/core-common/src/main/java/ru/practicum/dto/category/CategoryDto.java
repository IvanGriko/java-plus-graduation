package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.CreateOrUpdateValidator;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDto {

    Long id;

    @NotBlank(
            message = "Имя категории обязательно",
            groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class}
    )
    @Size(
            min = 1,
            max = 50,
            message = "Длина имени категории должна быть от 1 до 50 символов",
            groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class}
    )
    String name;
}