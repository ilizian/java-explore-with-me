package ru.practicum.explore.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ApiError {
    private String message;
    private String reason;
    private String status;
    private String timestamp;
    private List<String> errors;
}