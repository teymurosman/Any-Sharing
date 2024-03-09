package ru.practicum.shareit.booking.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBookerId(Long bookerId, Sort sort);

    Collection<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long bookerId, LocalDateTime time1, LocalDateTime time2, Sort sort);

    Collection<Booking> findByBookerIdAndEndLessThan(Long bookerId, LocalDateTime time, Sort sort);

    Collection<Booking> findByBookerIdAndStartGreaterThan(Long bookerId, LocalDateTime time, Sort sort);

    Collection<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus status, Sort sort);

    Collection<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    Collection<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long ownerId, LocalDateTime time1, LocalDateTime time2, Sort sort);

    Collection<Booking> findByItemOwnerIdAndEndLessThan(Long ownerId, LocalDateTime time, Sort sort);

    Collection<Booking> findByItemOwnerIdAndStartGreaterThan(Long ownerId, LocalDateTime time, Sort sort);

    Collection<Booking> findByItemOwnerIdAndStatusIs(Long ownerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime endTime);
}
