package ru.practicum.shareit.booking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CustomBadRequestException;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        booker = User.builder().id(1L).name("Booker").email("booker@example.com").build();
        owner = User.builder().id(2L).name("Owner").email("owner@example.com").build();
        item = Item.builder().id(1L).name("Item").description("Description").available(true).owner(owner).build();
        bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(item.getId())
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        bookingResponseDto = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(item).booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void testSaveBookingWhenAllConditionsAreMetThenBookingResponseDtoIsReturned() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.saveBooking(booker.getId(), bookingRequestDto);

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);
        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testSaveBookingWhenUserDoesNotExistThenThrowCustomEntityNotFoundException() {
        Long bookerId = 1L;
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.saveBooking(bookerId, bookingRequestDto),
                "User not exists");
        verify(userRepository).findById(bookerId);
        verifyNoInteractions(itemRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void testSaveBookingWhenItemDoesNotExistThenThrowCustomEntityNotFoundException() {
        Long bookerId = 1L;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.saveBooking(bookerId, bookingRequestDto),
                "Item not exists");
        verify(userRepository).findById(bookerId);
        verify(itemRepository).findById(bookingRequestDto.getItemId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void testSaveBookingWhenItemIsUnavailableThenThrowCustomBadRequestException() {
        Long bookerId = 1L;
        item.setAvailable(false);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(CustomBadRequestException.class, () -> bookingService.saveBooking(bookerId, bookingRequestDto),
                "Item is unavailable");
        verify(userRepository).findById(bookerId);
        verify(itemRepository).findById(bookingRequestDto.getItemId());
    }

    @Test
    void testSaveBookingWhenOwnerBooksHisItemThenThrowCustomEntityNotFoundException() {
        Long bookerId = owner.getId();
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.saveBooking(bookerId, bookingRequestDto),
                "Owner cannot book his item");
        verify(userRepository).findById(bookerId);
        verify(itemRepository).findById(bookingRequestDto.getItemId());
    }

    @Test
    void testUpdateBookingWhenAllConditionsAreMetThenBookingResponseDtoIsReturned() {
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(booking.getId())).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBooking(owner.getId(), booking.getId(), true);

        assertNotNull(result);
        assertEquals(bookingResponseDto.getId(), result.getId());
        assertEquals(bookingResponseDto.getStart(), result.getStart());
        assertEquals(bookingResponseDto.getEnd(), result.getEnd());
        assertEquals(bookingResponseDto.getItem().getId(), result.getItem().getId());
        assertEquals(bookingResponseDto.getBooker().getId(), result.getBooker().getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(booking.getId());
        verify(bookingRepository).save(booking);
    }

    @Test
    void testUpdateBookingWhenWrongOwnerIdThenThrowCustomEntityNotFoundException() {
        Long wrongOwnerId = 3L;
        Long bookingId = booking.getId();
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId)).thenReturn(booking);

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.updateBooking(wrongOwnerId, bookingId, true),
                "Wrong owner id");
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(bookingId);
    }

    @Test
    void testUpdateBookingWhenStatusIsNotWaitingThenThrowCustomBadRequestException() {
        Long ownerId = owner.getId();
        Long bookingId = booking.getId();
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId)).thenReturn(booking);

        assertThrows(CustomBadRequestException.class, () -> bookingService.updateBooking(ownerId, bookingId, true),
                "Status cannot be changed if status is not WAITING");
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(bookingId);
    }

    @Test
    void testGetBookingByBookingIdWhenAllConditionsAreMetThenBookingResponseDtoIsReturned() {
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(booking.getId())).thenReturn(booking);

        BookingResponseDto result = bookingService.getBookingByBookingId(booker.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(bookingResponseDto, result);
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(booking.getId());
    }

    @Test
    void testGetBookingByBookingIdWhenBookingDoesNotExistThenThrowCustomEntityNotFoundException() {
        Long userId = 1L;
        Long bookingId = 1L;
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId)).thenReturn(null);

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.getBookingByBookingId(userId, bookingId),
                "Booking not exist");
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(bookingId);
    }

    @Test
    void testGetBookingByBookingIdWhenUserIsNeitherBookerNorOwnerThenThrowCustomEntityNotFoundException() {
        Long userId = 3L;
        Long bookingId = booking.getId();
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId)).thenReturn(booking);

        assertThrows(CustomEntityNotFoundException.class, () -> bookingService.getBookingByBookingId(userId, bookingId),
                "Entity not found!");
        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(bookingId);
    }

    @Test
    void testGetBookingByBookingIdWhenUserIsOwnerThenReturnBookingResponseDto() {
        Long ownerId = owner.getId();
        Long bookingId = booking.getId();
        when(bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId)).thenReturn(booking);

        BookingResponseDto result = bookingService.getBookingByBookingId(ownerId, bookingId);

        assertNotNull(result, "Returned BookingResponseDto should not be null");
        assertEquals(booking.getId(), result.getId(), "Booking ID should match");
        assertEquals(booking.getStart(), result.getStart(), "Start time should match");
        assertEquals(booking.getEnd(), result.getEnd(), "End time should match");
        assertEquals(booking.getItem().getId(), result.getItem().getId(), "Item ID should match");
        assertEquals(booking.getBooker().getId(), result.getBooker().getId(), "Booker ID should match");
        assertEquals(booking.getStatus(), result.getStatus(), "Booking status should match");

        verify(bookingRepository).findBookingByIdWithItemAndBookerEagerly(bookingId);
    }

    @Test
    void testGetBookingByBookerIdWhenAllConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findAllByGivenUserId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findAllByGivenUserId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenAllAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "ALL";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByGivenUserId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByGivenUserId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenCURRENTConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "CURRENT";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findCurrentBookingsByBookerId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findCurrentBookingsByBookerId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenCURRENTAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "CURRENT";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookingsByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findCurrentBookingsByBookerId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenPASTConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "PAST";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findPastBookingsByBookerId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findPastBookingsByBookerId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenPASTAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "PAST";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findPastBookingsByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findPastBookingsByBookerId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenFUTUREConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findFutureBookingsByBookerId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findFutureBookingsByBookerId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenFUTUREAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "FUTURE";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findFutureBookingsByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findFutureBookingsByBookerId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenWAITINGConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findWaitingBookingsByBookerId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findWaitingBookingsByBookerId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenWAITINGAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "WAITING";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findWaitingBookingsByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findWaitingBookingsByBookerId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenREJECTEDConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "REJECTED";
        Integer from = 0;
        Integer size = 10;
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findRejectedBookingsByBookerId(eq(bookerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).findRejectedBookingsByBookerId(eq(bookerId), any(Pageable.class));
    }

    @Test
    void testGetBookingByBookerIdWhenREJECTEDAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long bookerId = 1L;
        String state = "REJECTED";
        Integer from = null;
        Integer size = null;
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.of(booker));
        when(bookingRepository.findRejectedBookingsByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(bookerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository).findRejectedBookingsByBookerId(bookerId);
    }

    @Test
    void testGetBookingByBookerIdWhenUnknownStateThenThrowException() {
        Long bookerId = 1L;
        String state = "UNKNOWN_STATE";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        assertThrows(IllegalStateException.class, () -> bookingService.getBookingByBookerId(bookerId, state, from, size));
    }

    @Test
    void testGetBookingByOwnerIdWhenAllConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findAllBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenAllAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "ALL";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findAllBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenCURRENTConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "CURRENT";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findCurrentBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findCurrentBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenCURRENTAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "CURRENT";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findCurrentBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findCurrentBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenPASTConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "PAST";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findPastBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findPastBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenPASTAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "PAST";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findPastBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findPastBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenFUTUREConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "FUTURE";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findFutureBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findFutureBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenFUTUREAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "FUTURE";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findFutureBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findFutureBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenWAITINGConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findWaitingBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findWaitingBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenWAITINGAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "WAITING";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findWaitingBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findWaitingBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenREJECTEDConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "REJECTED";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        var page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(booking));
        when(bookingRepository.findRejectedBookingsByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findRejectedBookingsByOwnerId(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void testGetBookingByOwnerIdWhenREJECTEDAndNoPageableConditionsAreMetThenListOfBookingResponseDtoIsReturned() {
        Long ownerId = 2L;
        String state = "REJECTED";
        Integer from = null;
        Integer size = null;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findRejectedBookingsByOwnerId(eq(ownerId))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(ownerId, state, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(bookingResponseDto, result.get(0));
        verify(userRepository).findById(owner.getId());
        verify(bookingRepository).findRejectedBookingsByOwnerId(eq(owner.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenUnknownStateThenThrowException() {
        Long ownerId = 2L;
        String state = "UNKNOWN_STATE";
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        assertThrows(IllegalStateException.class, () -> bookingService.getBookingByOwnerId(ownerId, state, from, size));
    }
}