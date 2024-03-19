package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, CustomEntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseErrorDto handleNotFoundException(final RuntimeException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return new ResponseErrorDto("Entity not found", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseErrorDto handleConflict(final ConflictException e) {
        log.warn("Conflict with server status: {}", e.getMessage());
        return new ResponseErrorDto("Conflict with server status", e.getMessage());
    }

    @ExceptionHandler({
            CustomBadRequestException.class,
            ConstraintViolationException.class,
            ConversionFailedException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseErrorDto handleBadRequest(final RuntimeException e) {
        log.warn("Bad request received: {}", e.getMessage());
        return new ResponseErrorDto(null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseErrorDto handleGeneralException(final Exception e) {
        log.error("An unexpected error message: {}", e.getMessage(), e);
        return new ResponseErrorDto("Internal Server Error", "An unexpected error");
    }
}