package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserRequestDto {

    @NotBlank(message = "Поле 'email' не должно быть пустым")
    @Email(message = "Поле 'email' должно соответствовать маске адреса электронной почты")
    @Size(min = 6, max = 254, message = "Длина поля 'email' должна составлять от 6 до 254 символов")
    String email;

    @NotBlank(message = "Поле 'name' не должно быть пустым")
    @Size(min = 2, max = 250, message = "Длина поля 'name' должна составлять от 2 до 250 символов")
    String name;

}
