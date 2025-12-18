package ru.practicum.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.api.user.UserApi;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceInteractionException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class UserClientAbstractHelper {

    protected final UserApi userApiClient;

    public UserShortDto fetchUserShortDtoByUserIdOrFail(Long userId) {
        try {
            return userApiClient.getUserShort(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new ServiceInteractionException("Не удалось получить информацию о пользователе с ID " + userId,
                    "Сервис пользователей недоступен");
        }
    }

    public UserShortDto fetchUserShortDtoByUserId(Long userId) {
        try {
            return userApiClient.getUserShort(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return UserShortDto.withOnlyId(userId);
        }
    }

    public Map<Long, UserShortDto> fetchUserShortDtoMapByUserIdList(Collection<Long> userIdList) {
        try {
            return userApiClient.getUserShortDtoListByIds(userIdList).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (RuntimeException e) {
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return userIdList.stream()
                    .collect(Collectors.toMap(id -> id, UserShortDto::withOnlyId));
        }
    }

    public UserDto fetchUserDtoByUserIdOrFail(Long userId) {
        try {
            return userApiClient.getUser(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new ServiceInteractionException("Не удалось получить информацию о пользователе с ID " + userId,
                    "Сервис пользователей недоступен");
        }
    }

    public UserDto fetchUserDtoByUserId(Long userId) {
        try {
            return userApiClient.getUser(userId);
        } catch (RuntimeException e) {
            if (isNotFoundCode(e)) throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return UserDto.withOnlyId(userId);
        }
    }

    public Map<Long, UserDto> fetchUserDtoMapByUserIdList(Collection<Long> userIdList) {
        try {
            return userApiClient.getUserDtoListByIds(userIdList).stream()
                    .collect(Collectors.toMap(UserDto::getId, u -> u));
        } catch (RuntimeException e) {
            log.warn("Ошибка взаимодействия с сервисом: поймано исключение {}. Причина: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            return userIdList.stream()
                    .collect(Collectors.toMap(id -> id, UserDto::withOnlyId));
        }
    }

    private boolean isNotFoundCode(RuntimeException e) {
        if (e instanceof FeignException.NotFound) return true;
        if (e.getCause() != null && e.getCause() instanceof FeignException.NotFound) return true;
        return false;
    }

}