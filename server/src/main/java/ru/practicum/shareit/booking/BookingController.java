package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingFromRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingResponse add(@RequestBody BookingFromRequest bookingFromRequest,
                               @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.add(bookingMapper.toBooking(bookingFromRequest), bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse approve(@PathVariable Long bookingId, @RequestParam(name = "approved") Boolean approved,
                                   @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approve(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getByBookingId(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getByBookingId(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingResponse> getByBookerId(
            @RequestParam(name = "state") StateFilter state,
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @RequestParam(name = "from") int from,
            @RequestParam(name = "size") int size) {
        return bookingService.getByBookerId(state, bookerId, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getByOwnerId(
            @RequestParam(name = "state") StateFilter state,
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(name = "from") int from,
            @RequestParam(name = "size") int size) {
        return bookingService.getByOwnerId(state, ownerId, from, size);
    }
}
