package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequestDto newUserRequestDto) {
        log.info("Создана новая учетная запись пользователя с именем: {}", newUserRequestDto.getName());
        return userService.create(newUserRequestDto);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive(message = "Идентификатор пользователя невалидный") Long userId) {
        log.info("Удаление пользователя с идентификатором: {}", userId);
        userService.delete(userId);
    }

    @GetMapping("/admin/users")
    public Collection<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Получение списка пользователей с параметрами: IDs={}, Начало индекса={}, Размер выборки={}", ids, from, size);
        return userService.findByIdListWithOffsetAndLimit(ids, from, size);
    }
}
