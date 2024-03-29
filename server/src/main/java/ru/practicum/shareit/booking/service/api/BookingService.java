package ru.practicum.shareit.booking.service.api;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto saveBooking(Long bookerId, BookingRequestDto requestDto);

    BookingResponseDto updateBooking(Long bookerId, Long bookingId, Boolean bookingStatus);

    BookingResponseDto getBookingByBookingId(Long id, Long bookingId);

    List<BookingResponseDto> getBookingsByBookerId(Long bookerId, String state, Integer from, Integer size);

    List<BookingResponseDto> getBookingsByOwnerId(Long ownerId, String state, Integer from, Integer size);
}
