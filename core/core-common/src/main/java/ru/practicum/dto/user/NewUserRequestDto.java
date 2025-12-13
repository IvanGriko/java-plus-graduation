package ru.practicum.dto.user;

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

    @NotBlank(message = "Электронная почта обязательна")
    @Email(message = "Электронная почта должна соответствовать формату адреса электронной почты")
    @Size(min = 6, max = 254, message = "Электронная почта должна быть от 6 до 254 символов")
    String email;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 250, message = "Имя должно быть от 2 до 250 символов")
    String name;

}
