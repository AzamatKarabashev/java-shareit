package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.api.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final BookingService service;

    @PostMapping
    public BookingResponseDto saveBooking(@RequestHeader(USER_ID) Long bookerId,
                                          @Valid @RequestBody BookingRequestDto requestDto) {
        log.debug("POST request in booking controller with booker id={}", bookerId);
        return service.saveBooking(bookerId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBooking(@RequestHeader(USER_ID) Long ownerId,
                                            @PathVariable Long bookingId,
                                            @RequestParam(name = "approved") Boolean bookingStatus) {
        log.debug("PATCH request in booking controller with booker id={}", ownerId);
        return service.updateBooking(ownerId, bookingId, bookingStatus);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingByBookingId(@RequestHeader(USER_ID) Long id,
                                                    @PathVariable Long bookingId) {
        log.debug("GET request received in booking controller to get booking by given booking id={}", id);
        return service.getBookingByBookingId(id, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingByBookerId(@RequestHeader(USER_ID) Long bookerId,
                                                         @RequestParam(required = false, defaultValue = "ALL") String state,
                                                         @RequestParam(required = false) @PositiveOrZero Integer from,
                                                         @RequestParam(required = false) @Positive Integer size) {
        log.debug("GET request received in booking controller to give list of booking by given booker id={}", bookerId);
        return service.getBookingByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingByOwnerId(@RequestHeader(USER_ID) Long ownerId,
                                                        @RequestParam(required = false, defaultValue = "ALL") String state,
                                                        @RequestParam(required = false) @PositiveOrZero Integer from,
                                                        @RequestParam(required = false) @Positive Integer size) {
        log.debug("GET request received in booking controller to give list of booking by given owner id={}", ownerId);
        return service.getBookingByOwnerId(ownerId, state, from, size);
    }
}