package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.by;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = false)
    public UserDto create(NewUserRequestDto newUserRequestDto) {
        log.info("Создаётся новый пользователь с почтой: {}", newUserRequestDto.getEmail());
        if (userRepository.existsByEmail(newUserRequestDto.getEmail())) {
            throw new ConflictException("Пользователь с указанной почтой уже существует",
                    "Нарушено ограничение целостности данных");
        }
        User newUser = UserMapper.toEntity(newUserRequestDto);
        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при попытке сохранить пользователя: {}", e.getMessage());
            throw new ConflictException("Не удалось создать пользователя", "Ошибка сервера");
        }
        return UserMapper.toDto(newUser);
    }

    @Transactional(readOnly = false)
    public void delete(Long userId) {
        log.info("Удаляется пользователь с идентификатором: {}", userId);
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным идентификатором не найден"));
        userRepository.delete(userToDelete);
    }

    public List<UserDto> findByIdListWithOffsetAndLimit(List<Long> idList, Integer from, Integer size) {
        log.info("Получение списка пользователей с параметрами: idList={}, from={}, size={}", idList, from, size);
        if (idList == null || idList.isEmpty()) {
            return userRepository.findAll(of(from / size, size, by("id")))
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
}

//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    @Transactional(readOnly = false)
//    public UserDto create(NewUserRequestDto newUserRequestDto) {
//        if (userRepository.existsByEmail(newUserRequestDto.getEmail())) {
//            throw new ConflictException("User with email " + newUserRequestDto.getEmail() + " already exists",
//                    "Integrity constraint has been violated");
//        }
//        User newUser = UserMapper.toEntity(newUserRequestDto);
//        userRepository.save(newUser);
//        return UserMapper.toDto(newUser);
//    }
//
//    @Transactional(readOnly = false)
//    public void delete(Long userId) {
//        User userToDelete = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
//        userRepository.delete(userToDelete);
//    }
//
//    public List<UserDto> findByIdListWithOffsetAndLimit(List<Long> idList, Integer from, Integer size) {
//        if (idList == null || idList.isEmpty()) {
//            Sort sort = Sort.by(Sort.Direction.ASC, "id");
//            return userRepository.findAll(PageRequest.of(from / size, size, sort))
//                    .stream()
//                    .map(UserMapper::toDto)
//                    .toList();
//        } else {
//            return userRepository.findAllById(idList)
//                    .stream()
//                    .map(UserMapper::toDto)
//                    .toList();
//        }
//    }
//}
