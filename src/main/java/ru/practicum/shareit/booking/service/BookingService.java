package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StateFilter;

import java.util.Collection;

public interface BookingService {

    BookingResponse add(Booking booking, Long bookerId);

    BookingResponse approve(Long bookingId, Boolean approved, Long ownerId);

    BookingResponse getByBookingId(Long bookingId, Long userId);

    Collection<BookingResponse> getByBookerId(StateFilter state, Long bookerId, int from, int size);

    Collection<BookingResponse> getByOwnerId(StateFilter state, Long ownerId, int from, int size);
}
