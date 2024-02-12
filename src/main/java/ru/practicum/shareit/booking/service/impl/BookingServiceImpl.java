package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enumeration.BookingState;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.api.BookingService;
import ru.practicum.shareit.exception.CustomBadRequestException;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto saveBooking(Long bookerId, BookingRequestDto requestDto) {
        bookingTimeValidation(requestDto);
        User booker = userRepository.findById(bookerId)
                .orElseThrow((() -> new CustomEntityNotFoundException("User not exists")));
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow((() -> new CustomEntityNotFoundException("Item not exists")));
        if (!item.getAvailable()) {
            throw new CustomBadRequestException("Item is unavailable");
        }
        if (Objects.equals(item.getOwner().getId(), bookerId)) {
            throw new CustomEntityNotFoundException("Owner cannot book his item");
        }
        Booking bookingAfterMap = toBooking(requestDto, item, booker);
        Booking savedBooking = bookingRepository.save(bookingAfterMap);
        return toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto updateBooking(Long ownerId, Long bookingId, Boolean approveStatus) {
        Booking booking = bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new CustomEntityNotFoundException("Wrong owner id");
        }
        if (booking.getStatus().equals(BookingStatus.WAITING)) {
            BookingStatus newStatus = approveStatus ? BookingStatus.APPROVED : BookingStatus.REJECTED;
            booking.setStatus(newStatus);
        } else {
            throw new CustomBadRequestException("Status cannot be changed if status is not WAITING");
        }
        Booking savedBooking = bookingRepository.save(booking);
        return toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto getBookingByBookingId(Long id, Long bookingId) {
        Booking booking = bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId);
        if (booking == null) {
            throw new CustomEntityNotFoundException("Booking not exist");
        }
        boolean isBooker = Objects.equals(booking.getBooker().getId(), id);
        boolean isOwner = Objects.equals(booking.getItem().getOwner().getId(), id);
        if (isBooker) {
            return toBookingResponseDto(booking);
        } else if (isOwner) {
            return toBookingResponseDto(booking);
        } else {
            throw new CustomEntityNotFoundException("Entity not found!");
        }
    }

    @Override
    public List<BookingResponseDto> getBookingByBookerId(Long bookerId, String state) {
        userRepository.findById(bookerId).orElseThrow(() -> new CustomEntityNotFoundException("Booker not exist"));
        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Unknown state: " + state);
        }
        switch (fromState) {
            case ALL:
                return toBookingResponseDtoList(bookingRepository.findAllByGivenUserId(bookerId));
            case CURRENT:
                return toBookingResponseDtoList(bookingRepository.findCurrentBookingsByBookerId(bookerId));
            case PAST:
                return toBookingResponseDtoList(bookingRepository.findPastBookingsByBookerId(bookerId));
            case FUTURE:
                return toBookingResponseDtoList(bookingRepository.findFutureBookingsByBookerId(bookerId));
            case WAITING:
                return toBookingResponseDtoList(bookingRepository.findWaitingBookingsByBookerId(bookerId));
            case REJECTED:
                return toBookingResponseDtoList(bookingRepository.findRejectedBookingsByBookerId(bookerId));
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingResponseDto> getBookingByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new CustomEntityNotFoundException("Owner not exists"));
        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Unknown state: " + state);
        }
        switch (fromState) {
            case ALL:
                return toBookingResponseDtoList(bookingRepository.findAllBookingsByOwnerId(ownerId));
            case CURRENT:
                return toBookingResponseDtoList(bookingRepository.findCurrentBookingsByOwnerId(ownerId));
            case PAST:
                return toBookingResponseDtoList(bookingRepository.findPastBookingsByOwnerId(ownerId));
            case FUTURE:
                return toBookingResponseDtoList(bookingRepository.findFutureBookingsByOwnerId(ownerId));
            case WAITING:
                return toBookingResponseDtoList(bookingRepository.findWaitingBookingsByOwnerId(ownerId));
            case REJECTED:
                return toBookingResponseDtoList(bookingRepository.findRejectedBookingsByOwnerId(ownerId));
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    private static void bookingTimeValidation(BookingRequestDto requestDto) {
        if (requestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new CustomBadRequestException("Start time must be in future");
        }
        if (requestDto.getStart().equals(requestDto.getEnd())) {
            throw new CustomBadRequestException("Start time must be equal end time");
        }
        if (requestDto.getStart().isAfter(requestDto.getEnd())) {
            throw new CustomBadRequestException("Start time must be before end time");
        }
    }
}