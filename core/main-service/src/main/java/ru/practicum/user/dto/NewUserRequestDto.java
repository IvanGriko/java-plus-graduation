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

    @NotBlank(message = "Field 'email' shouldn't be blank")
    @Email(message = "Field 'email' should match email mask")
    @Size(min = 6, max = 254, message = "Field 'email' should be from 6 to 254 characters")
    String email;

    @NotBlank(message = "Field 'name' shouldn't be blank")
    @Size(min = 2, max = 250, message = "Field 'name' should be from 2 to 250 characters")
    String name;

}
