package ru.practicum.shareit.exception;

import lombok.Data;

@Data
public class ResponseErrorDto {
    private final String message;
    private final String errorMessage;
}