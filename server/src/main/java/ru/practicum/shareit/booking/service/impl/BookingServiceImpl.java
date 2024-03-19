package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enumeration.BookingState;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.api.BookingService;
import ru.practicum.shareit.exception.CustomBadRequestException;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto saveBooking(Long bookerId, BookingRequestDto requestDto) {
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
        Booking bookingAfterMap = BookingMapper.INSTANCE.toBooking(requestDto, item, booker);
        Booking savedBooking = bookingRepository.save(bookingAfterMap);
        return BookingMapper.INSTANCE.toBookingResponseDto(savedBooking);
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
        return BookingMapper.INSTANCE.toBookingResponseDto(savedBooking);
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
            return BookingMapper.INSTANCE.toBookingResponseDto(booking);
        } else if (isOwner) {
            return BookingMapper.INSTANCE.toBookingResponseDto(booking);
        } else {
            throw new CustomEntityNotFoundException("Entity not found!");
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsByBookerId(Long bookerId, String state, Integer from, Integer size) {
        userRepository.findById(bookerId).orElseThrow(() -> new CustomEntityNotFoundException("Booker not exist"));
        BookingState fromState = null;
        boolean isStateValid = true;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            isStateValid = false;
        }
        if (isStateValid) {
            Pageable pageable = PageRequest.of(from / size, size);
            switch (fromState) {
                case ALL:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findAllByGivenUserId(bookerId, pageable).getContent());
                case CURRENT:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findCurrentBookingsByBookerId(bookerId, pageable).getContent());
                case PAST:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findPastBookingsByBookerId(bookerId, pageable).getContent());
                case FUTURE:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findFutureBookingsByBookerId(bookerId, pageable).getContent());
                case WAITING:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findWaitingBookingsByBookerId(bookerId, pageable).getContent());
                case REJECTED:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findRejectedBookingsByBookerId(bookerId, pageable).getContent());
            }
        }
        throw new IllegalStateException("Unknown state: " + state);
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwnerId(Long ownerId, String state, Integer from, Integer size) {
        userRepository.findById(ownerId).orElseThrow(() -> new CustomEntityNotFoundException("Owner not exists"));
        BookingState fromState = null;
        boolean isStateValid = true;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            isStateValid = false;
        }
        if (isStateValid) {
            Pageable pageable = PageRequest.of(from / size, size);
            switch (fromState) {
                case ALL:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findAllBookingsByOwnerId(ownerId, pageable).getContent());
                case CURRENT:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findCurrentBookingsByOwnerId(ownerId, pageable).getContent());
                case PAST:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findPastBookingsByOwnerId(ownerId, pageable).getContent());
                case FUTURE:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findFutureBookingsByOwnerId(ownerId, pageable).getContent());
                case WAITING:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findWaitingBookingsByOwnerId(ownerId, pageable).getContent());
                case REJECTED:
                    return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                            .findRejectedBookingsByOwnerId(ownerId, pageable).getContent());
            }

        }
        throw new IllegalStateException("Unknown state: " + state);
    }
}