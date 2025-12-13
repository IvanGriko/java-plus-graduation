package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.user.UserApi;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public UserDto createUser(NewUserRequestDto newUserRequestDto) {
        log.info("Создание нового пользователя");
        return userService.create(newUserRequestDto);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID {}", userId);
        userService.delete(userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Получение информации о пользователе с ID {}", userId);
        return userService.get(userId);
    }

    @Override
    public UserShortDto getUserShort(Long userId) {
        log.info("Получение короткой информации о пользователе с ID {}", userId);
        return userService.getShort(userId);
    }

    @Override
    public Collection<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей по списку ID {}", ids);
        return userService.findUsersByIdsWithPaging(ids, from, size);
    }

    @Override
    public Collection<UserShortDto> getUserShortDtoListByIds(Collection<Long> ids) {
        log.info("Получение короткого списка пользователей по ID {}", ids);
        return userService.findUserShortDtoListByIds(ids);
    }

    @Override
    public Collection<UserDto> getUserDtoListByIds(Collection<Long> ids) {
        log.info("Получение полного списка пользователей по ID {}", ids);
        return userService.findUserDtoListByIds(ids);
    }

}