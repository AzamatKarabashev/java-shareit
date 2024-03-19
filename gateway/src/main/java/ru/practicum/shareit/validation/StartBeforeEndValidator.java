package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, BookingRequestDto> {
    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        } else if (dto.getStart() == null) {
            return true;
        } else if (dto.getEnd() == null) {
            return true;
        }
        return dto.getStart().isBefore(dto.getEnd());
    }
}