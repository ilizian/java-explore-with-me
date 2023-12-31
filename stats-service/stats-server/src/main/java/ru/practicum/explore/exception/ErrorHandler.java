package ru.practicum.explore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleThrowableExceptions(final Throwable e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace())
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(final ValidationException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace())
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.toString())
                .build();
    }
}