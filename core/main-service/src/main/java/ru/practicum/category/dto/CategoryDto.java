package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.CreateOrUpdateValidator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDto {

    Long id;

    @NotBlank(groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class},
            message = "Название категории не должно быть пустым")
    @Size(min = 1, max = 50, groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class},
            message = "Длина названия должна быть от 1 до 50 символов")
    String name;

}

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class CategoryDto {
//
//    Long id;
//
//    @NotBlank(groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
//    @Size(min = 1, max = 50, groups = {CreateOrUpdateValidator.Create.class, CreateOrUpdateValidator.Update.class})
//    String name;
//}