package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dal.User;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = false)
    public UserDto create(NewUserRequestDto newUserRequestDto) {
        log.info("Регистрация нового пользователя с почтой {}", newUserRequestDto.getEmail());
        if (userRepository.existsByEmail(newUserRequestDto.getEmail())) {
            log.error("Пользователь с почтой {} уже зарегистрирован", newUserRequestDto.getEmail());
            throw new ConflictException("Пользователь с указанной почтой уже существует", "Нарушение уникальности");
        }
        User newUser = UserMapper.toNewEntity(newUserRequestDto);
        userRepository.save(newUser);
        return UserMapper.toDto(newUser);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Long userId) {
        log.info("Удаление пользователя с ID {}", userId);
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        userRepository.delete(userToDelete);
    }

    @Override
    public UserDto get(Long userId) {
        log.info("Получение информации о пользователе с ID {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toDto(user);
    }

    @Override
    public UserShortDto getShort(Long userId) {
        log.info("Получение краткой информации о пользователе с ID {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toUserShortDto(user);
    }

    @Override
    public List<UserDto> findUsersByIdsWithPaging(List<Long> idList, Integer from, Integer size) {
        log.info("Получение пользователей по списку ID {}", idList);
        if (idList == null || idList.isEmpty()) {
            Sort sort = Sort.by(Sort.Direction.ASC, "id");
            return userRepository.findAll(PageRequest.of(from / size, size, sort))
                    .stream()
                    .map(UserMapper::toDto)
                    .toList();
        } else {
            return userRepository.findAllById(idList)
                    .stream()
                    .map(UserMapper::toDto)
                    .toList();
        }
    }

    @Override
    public Collection<UserShortDto> findUserShortDtoListByIds(Collection<Long> idList) {
        log.info("Получение краткой информации о пользователях по списку ID {}", idList);
        if (idList == null || idList.isEmpty()) return List.of();
        return userRepository.findAllById(idList).stream()
                .map(UserMapper::toUserShortDto)
                .toList();
    }

    @Override
    public Collection<UserDto> findUserDtoListByIds(Collection<Long> idList) {
        log.info("Получение полной информации о пользователях по списку ID {}", idList);
        if (idList == null || idList.isEmpty()) return List.of();
        return userRepository.findAllById(idList).stream()
                .map(UserMapper::toDto)
                .toList();
    }

}