package ru.practicum.shareit.exception;

public class CustomEntityNotFoundException extends RuntimeException {
    public CustomEntityNotFoundException(String s) {
        super(s);
    }
}
