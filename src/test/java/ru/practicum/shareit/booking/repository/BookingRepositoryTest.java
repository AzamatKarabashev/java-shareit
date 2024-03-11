package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    Booking booking = new Booking();
    Item item = new Item();
    User user = new User();

    @BeforeEach
    void setUp() {
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);
        itemRepository.save(item);

        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    @Test
    void testInsertActionInRepository() {
        Booking savedBooking = bookingRepository.findById(booking.getId()).orElse(null);

        assertNotNull(savedBooking);

        assertEquals(savedBooking.getId(), booking.getId());
    }

    @Test
    void testFindBookingByIdWithItemAndBookerEagerly() {
        Booking foundBooking = bookingRepository.findBookingByIdWithItemAndBookerEagerly(booking.getId());

        assertThat(foundBooking).isNotNull();
        assertThat(foundBooking.getItem()).isEqualTo(item);
        assertThat(foundBooking.getBooker()).isEqualTo(user);
    }

    @Test
    void testFindAllByGivenUserId() {
        List<Booking> foundBookings = bookingRepository.findAllByGivenUserId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem()).isEqualTo(item);
        assertThat(foundBookings.get(0).getBooker()).isEqualTo(user);
        assertThat(foundBookings.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void testFindAllByGivenUserIdWithPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findAllByGivenUserId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void testFindCurrentBookingsByBookerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = new Booking(null, now.minusHours(1), now.plusHours(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        List<Booking> foundBookings = bookingRepository.findCurrentBookingsByBookerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void testFindCurrentBookingsByBookerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findCurrentBookingsByBookerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void testFindPastBookingsByBookerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<Booking> foundBookings = bookingRepository.findPastBookingsByBookerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void testFindPastBookingsByBookerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);
        Page<Booking> foundBookingsPage = bookingRepository.findPastBookingsByBookerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void testFindFutureBookingsByBookerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item, user, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<Booking> foundBookings = bookingRepository.findFutureBookingsByBookerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getId()).isEqualTo(futureBooking.getId());
    }

    @Test
    void testFindFutureBookingsByBookerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item, user, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);
        Page<Booking> foundBookingsPage = bookingRepository.findFutureBookingsByBookerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getId()).isEqualTo(futureBooking.getId());
    }

    @Test
    void testFindWaitingBookingsByBookerId() {
        Booking waitingBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        List<Booking> foundBookings = bookingRepository.findWaitingBookingsByBookerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testFindWaitingBookingsByBookerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Booking waitingBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);
        Page<Booking> foundBookingsPage = bookingRepository.findWaitingBookingsByBookerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testFindRejectedBookingsByBookerId() {
        Booking rejectedBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        List<Booking> foundBookings = bookingRepository.findRejectedBookingsByBookerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFindRejectedBookingsByBookerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Booking rejectedBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);
        Page<Booking> foundBookingsPage = bookingRepository.findRejectedBookingsByBookerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFindAllBookingsByOwnerId() {
        List<Booking> foundBookings = bookingRepository.findAllBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindAllBookingsByOwnerIdByPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findAllBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindCurrentBookingsByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = new Booking(null, now.minusHours(1), now.plusHours(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        List<Booking> foundBookings = bookingRepository.findCurrentBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindCurrentBookingsByOwnerIdByPageable() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = new Booking(null, now.minusHours(1), now.plusHours(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findCurrentBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindPastBookingsByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<Booking> foundBookings = bookingRepository.findPastBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindPastBookingsByOwnerIdByPageable() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findPastBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindFutureBookingsByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item, user, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<Booking> foundBookings = bookingRepository.findFutureBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindFutureBookingsByOwnerIdByPageable() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findPastBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getItem().getOwner()).isEqualTo(user);
    }

    @Test
    void testFindWaitingBookingsByOwnerId() {
        Booking waitingBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        List<Booking> foundBookings = bookingRepository.findWaitingBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testFindWaitingBookingsByOwnerIdByPageable() {
        Booking waitingBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findWaitingBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testFindRejectedBookingsByOwnerId() {
        Booking rejectedBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        List<Booking> foundBookings = bookingRepository.findRejectedBookingsByOwnerId(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFindRejectedBookingsByOwnerIdPageable() {
        Booking rejectedBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, user, BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> foundBookingsPage = bookingRepository.findRejectedBookingsByOwnerId(user.getId(), pageable);

        assertThat(foundBookingsPage.getContent()).isNotNull();
        assertThat(foundBookingsPage.getContent().get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFindPastBookingsByItemId() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<Booking> foundBookings = bookingRepository.findPastBookingsByItemId(item.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void testFindFutureBookingsByItemId() {
        LocalDateTime now = LocalDateTime.now();
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item, user, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<Booking> foundBookings = bookingRepository.findFutureBookingsByItemId(item.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void testFindLastBookingsForOwnerItems() {
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(lastBooking);

        List<Booking> foundBookings = bookingRepository.findLastBookingsForOwnerItems(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    void testFindNextBookingsForOwnerItems() {
        LocalDateTime now = LocalDateTime.now();
        Booking nextBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item, user, BookingStatus.APPROVED);
        bookingRepository.save(nextBooking);

        List<Booking> foundBookings = bookingRepository.findNextBookingsForOwnerItems(user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    void testFindFinishedBookingsByItemAndUser() {
        LocalDateTime now = LocalDateTime.now();
        Booking finishedBooking = new Booking(null, now.minusDays(2), now.minusDays(1), item, user, BookingStatus.APPROVED);
        bookingRepository.save(finishedBooking);

        List<Booking> foundBookings = bookingRepository.findFinishedBookingsByItemAndUser(item.getId(), user.getId());

        assertThat(foundBookings).isNotNull();
        assertThat(foundBookings.get(0).getItem().getId()).isEqualTo(item.getId());
        assertThat(foundBookings.get(0).getBooker().getId()).isEqualTo(user.getId());
    }
}