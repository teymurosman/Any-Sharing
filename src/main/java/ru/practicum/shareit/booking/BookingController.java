package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingFromRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingResponse add(@Valid @RequestBody BookingFromRequest bookingFromRequest,
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
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @RequestParam(name = "from", defaultValue = "0")
                @PositiveOrZero(message = "Параметр начала не может быть отрицательным") int from,
            @RequestParam(name = "size", defaultValue = "10")
                @Positive(message = "Параметр размера страницы должен быть больше 0") int size) {
        return bookingService.getByBookerId(StateFilter.parseString(state), bookerId, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getByOwnerId(
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(name = "from", defaultValue = "0")
                @PositiveOrZero(message = "Параметр начала не может быть отрицательным") int from,
            @RequestParam(name = "size", defaultValue = "10")
                @Positive(message = "Параметр размера страницы должен быть больше 0") int size) {
        return bookingService.getByOwnerId(StateFilter.parseString(state), ownerId, from, size);
    }
}
