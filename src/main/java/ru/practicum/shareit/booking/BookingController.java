package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingResponse add(@Valid @RequestBody BookingCreateRequest bookingCreateRequest,
                               @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.add(bookingMapper.toBooking(bookingCreateRequest), bookerId);
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
    public Collection<BookingResponse> getByBookerId(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                      @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.getByBookerId(StateFilter.parseString(state), bookerId);
    }

    @GetMapping("/owner")
    public Collection<BookingResponse> getByOwnerId(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.getByOwnerId(StateFilter.parseString(state), ownerId);
    }
}
