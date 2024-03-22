package ru.practicum.shareit.booking.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Pageable page);

    List<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long bookerId, LocalDateTime time1, LocalDateTime time2, Pageable page);

    List<Booking> findByBookerIdAndEndLessThan(Long bookerId, LocalDateTime time, Pageable page);

    List<Booking> findByBookerIdAndStartGreaterThan(Long bookerId, LocalDateTime time, Pageable page);

    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus status, Pageable page);

    List<Booking> findByItemOwnerId(Long ownerId, Pageable page);

    List<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long ownerId, LocalDateTime time1, LocalDateTime time2, Pageable page);

    List<Booking> findByItemOwnerIdAndEndLessThan(Long ownerId, LocalDateTime time, Pageable page);

    List<Booking> findByItemOwnerIdAndStartGreaterThan(Long ownerId, LocalDateTime time, Pageable page);

    List<Booking> findByItemOwnerIdAndStatusIs(Long ownerId, BookingStatus status, Pageable page);

    Optional<Booking> findFirstByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime endTime);
}
