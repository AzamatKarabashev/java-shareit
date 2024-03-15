package ru.practicum.shareit.exception;

public class CustomBadRequestException extends RuntimeException {
    public CustomBadRequestException(String s) {
        super(s);
    }
}
