package ru.practicum.user.service;

import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto create(NewUserRequestDto newUserRequestDto);

    void delete(Long userId);

    UserDto get(Long userId);

    UserShortDto getShort(Long userId);

    List<UserDto> findUsersByIdsWithPaging(List<Long> idList, Integer from, Integer size);

    Collection<UserShortDto> findUserShortDtoListByIds(Collection<Long> idList);

    Collection<UserDto> findUserDtoListByIds(Collection<Long> idList);
}