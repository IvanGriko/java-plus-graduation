package ru.practicum.exception;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(BadRequestException e) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason(e.getReason())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e,
                                                              HttpServletRequest request) {
        Optional<String> message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst();
        log.info("Провал валидации: {}", message.orElse("Некорректный ввод"));
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Провал валидации")
                .message(message.orElse("Некорректный ввод"))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                          HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.info("Провал валидации: {}", errorMessage);
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Провал валидации")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException e,
                                                           HttpServletRequest request) {
        log.error("Возникла ошибка сервера: {}", e.getMessage(), e);
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Ошибка сервера")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleIllegalArguments(Exception e,
                                                           HttpServletRequest request) {
        log.info("Неправильный запрос: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Неправильный запрос")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingRequestHeaderException(MissingRequestHeaderException e,
                                                                        HttpServletRequest request) {
        log.info("Отсутствует требуемый заголовок: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Отсутствует заголовок")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException e,
                                                            HttpServletRequest request) {
        log.info("Конфликт ресурсов: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException e,
                                                             HttpServletRequest request) {
        log.info("Доступ запрещён: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason("Доступ запрещён")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e,
                                                            HttpServletRequest request) {
        log.info("Ресурс не найден: {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Ресурс не найден")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }
}