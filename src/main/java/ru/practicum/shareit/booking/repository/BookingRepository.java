package ru.practicum.shareit.booking.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    Collection<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId, LocalDateTime time1, LocalDateTime time2);

    Collection<Booking> findByBookerIdAndEndLessThanOrderByStartDesc(Long bookerId, LocalDateTime time);

    Collection<Booking> findByBookerIdAndStartGreaterThanOrderByStartDesc(Long bookerId, LocalDateTime time);

    Collection<Booking> findByBookerIdAndStatusIsOrderByStartDesc(Long bookerId, BookingStatus status);

    Collection<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    Collection<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime time1, LocalDateTime time2);

    Collection<Booking> findByItemOwnerIdAndEndLessThanOrderByStartDesc(Long ownerId, LocalDateTime time);

    Collection<Booking> findByItemOwnerIdAndStartGreaterThanOrderByStartDesc(Long ownerId, LocalDateTime time);

    Collection<Booking> findByItemOwnerIdAndStatusIsOrderByStartDesc(Long ownerId, BookingStatus status);

    Optional<Booking> findFirstByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime endTime);
}
