package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Для получения брони по быстрой (не ленивой) загрузке полей сущностей
    @Query("SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker WHERE b.id = :id")
    Booking findBookingByIdWithItemAndBookerEagerly(Long id);

    // Для получения всех заказов пользователя
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId ORDER BY b.start DESC")
    List<Booking> findAllByGivenUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId ORDER BY b.start DESC")
    Page<Booking> findAllByGivenUserId(@Param("userId") Long userId, Pageable pageable);

    // Для состояния CURRENT (текущие бронирования)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start <= CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start <= CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findCurrentBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    // Для состояния PAST (прошедшие бронирования)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findPastBookingsByBookerId(@Param("bookerId") Long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findPastBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    // Для состояния FUTURE (будущие бронирования)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByBookerId(@Param("bookerId") Long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findFutureBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    // Для состояния WAITING (бронирования, ожидающие подтверждения)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookingsByBookerId(@Param("bookerId") Long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.status = 'WAITING' ORDER BY b.start DESC")
    Page<Booking> findWaitingBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    // Для состояния REJECTED (отклоненные бронирования)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookingsByBookerId(@Param("bookerId") Long bookerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.status = 'REJECTED' ORDER BY b.start DESC")
    Page<Booking> findRejectedBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    // Для состояния ALL (все бронирования) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    Page<Booking> findAllBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Для состояния CURRENT (текущие бронирования) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start <= CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start <= CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findCurrentBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Для состояния PAST (прошедшие бронирования) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findPastBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findPastBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Для состояния FUTURE (будущие бронирования) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    List<Booking> findFutureBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Page<Booking> findFutureBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Для состояния WAITING (бронирования, ожидающие подтверждения) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findWaitingBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    Page<Booking> findWaitingBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Для состояния REJECTED (отклоненные бронирования) OWNER (ВЛАДЕЛЕЦ)
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findRejectedBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    Page<Booking> findRejectedBookingsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Метод для получения следующего будущего бронирования для вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.start < CURRENT_TIMESTAMP " +
            "AND NOT b.status = 'REJECTED' ORDER BY b.end DESC")
    List<Booking> findPastBookingsByItemId(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.start > CURRENT_TIMESTAMP " +
            "AND NOT b.status = 'REJECTED' ORDER BY b.start ASC")
    List<Booking> findFutureBookingsByItemId(@Param("itemId") Long itemId);

    // Оптимизированные методы для получения новых списков List<Item> с учетом last и next
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP ORDER BY b.end DESC")
    List<Booking> findLastBookingsForOwnerItems(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP ORDER BY b.start ASC")
    List<Booking> findNextBookingsForOwnerItems(@Param("ownerId") Long ownerId);

    // Имеет ли право на коммент юзер
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :userId AND b.end < CURRENT_TIMESTAMP")
    List<Booking> findFinishedBookingsByItemAndUser(@Param("itemId") Long itemId, @Param("userId") Long userId);
}
