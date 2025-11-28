package ru.practicum.category.dto;

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

    @NotBlank(groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
    @Size(min = 1, max = 50, groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
    String name;
}