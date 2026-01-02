package ru.practicum.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.*;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Ошибка валидации данных");
        log.warn("Ошибка валидации: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn("Ошибка валидации: {}", errorMessage);
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Ошибка валидации")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleBadArguments(
            Throwable e,
            HttpServletRequest request
    ) {
        log.warn("Некорректные аргументы запроса: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Некорректные аргументы запроса")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingRequestHeaderException(
            MissingRequestHeaderException e,
            HttpServletRequest request
    ) {
        log.warn("Отсутствует обязательный заголовок запроса: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Отсутствует обязательный заголовок запроса")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(
            BadRequestException e,
            HttpServletRequest request
    ) {
        log.warn("Ошибка запроса: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(
            ConflictException e,
            HttpServletRequest request
    ) {
        log.warn("Конфликт: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(
            ForbiddenException e,
            HttpServletRequest request
    ) {
        log.warn("Доступ запрещён: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(
            NotFoundException e,
            HttpServletRequest request
    ) {
        log.warn("Запрошенный ресурс не найден: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceInteractionException.class)
    public ResponseEntity<ApiError> handleServiceInteractionException(
            ServiceInteractionException e,
            HttpServletRequest request
    ) {
        log.warn("Ошибка взаимодействия с сервисом: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.SERVICE_UNAVAILABLE);
    }
}