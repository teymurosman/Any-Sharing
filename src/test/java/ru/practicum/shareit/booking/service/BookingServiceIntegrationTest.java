package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.exception.BookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    Booking booking1;

    Booking booking2;

    User user1;

    User user2;

    Item item1;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("Joe");
        user1.setEmail("joe@mail.com");

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");

        userService.add(user1);
        userService.add(user2);

        item1 = new Item();
        item1.setName("item name");
        item1.setDescription("item description");
        item1.setAvailable(true);

        itemService.add(user1.getId(), item1);

        booking1 = new Booking();
        booking1.setItem(item1);
        booking1.setStart(LocalDateTime.of(2160, 3, 10, 15, 16, 17));
        booking1.setEnd(LocalDateTime.of(2175, 5, 13, 18, 43, 42));

        booking2 = new Booking();
        booking2.setItem(item1);
        booking2.setStart(LocalDateTime.of(3260, 3, 10, 15, 16, 17));
        booking2.setEnd(LocalDateTime.of(3275, 5, 13, 18, 43, 42));
    }

    @Test
    void add_thenGetByBookingId_thenApprove_thenGetByBookerIdRejected_thenGetByOwnerIdFuture() {
        bookingService.add(booking1, user2.getId());

        BookingResponse createdBooking = bookingService.getByBookingId(booking1.getId(), user2.getId());

        assertEquals(1L, createdBooking.getId());
        assertEquals(booking1.getStart(), createdBooking.getStart());
        assertEquals(booking1.getEnd(), createdBooking.getEnd());
        assertEquals(BookingStatus.WAITING, createdBooking.getStatus());
        assertEquals(user2.getId(), createdBooking.getBooker().getId());
        assertEquals(user2.getName(), createdBooking.getBooker().getName());

        BookingResponse approvedBooking = bookingService.approve(booking1.getId(), true, user1.getId());

        assertEquals(booking1.getId(), approvedBooking.getId());
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());

        StateFilter stateRejected = StateFilter.REJECTED;
        int from = 0;
        int size = 10;
        List<BookingResponse> foundRejectedBookings =
                List.copyOf(bookingService.getByBookerId(stateRejected, user2.getId(), from, size));

        assertEquals(0, foundRejectedBookings.size());

        StateFilter stateFuture = StateFilter.FUTURE;
        List<BookingResponse> foundFutureBookings =
                List.copyOf(bookingService.getByOwnerId(stateFuture, user1.getId(), from, size));

        assertEquals(1, foundFutureBookings.size());
        assertEquals(booking1.getStart(), foundFutureBookings.get(0).getStart());
        assertTrue(foundFutureBookings.get(0).getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void add_startEqualsEnd() {
        booking1.setEnd(booking1.getStart());

        assertThrows(BookingException.class, () -> bookingService.add(booking1, user2.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getByBookingId(1L, user2.getId()));
    }

    @Test
    void add_itemNotAvailable() {
        Item notAddedItem = new Item();
        notAddedItem.setId(9999L);
        booking1.setItem(notAddedItem);

        assertThrows(EntityNotFoundException.class, () -> bookingService.add(booking1, user2.getId()));

        item1.setAvailable(false);
        itemService.update(item1.getId(), item1.getOwner().getId(), item1);
        booking1.setItem(item1);

        assertThrows(BookingException.class, () -> bookingService.add(booking1, user2.getId()));
    }

    @Test
    void add_userIsOwner() {
        final long ownerId = item1.getOwner().getId();

        assertThrows(ForbiddenAccessToEntityException.class, () -> bookingService.add(booking1, ownerId));
    }

    @Test
    void approve_notFromStatusWaiting() {
        bookingService.add(booking1, user2.getId());
        bookingService.approve(booking1.getId(), true, item1.getOwner().getId());

        assertThrows(BookingException.class,
                () -> bookingService.approve(booking1.getId(), false, item1.getOwner().getId()));
    }

    @Test
    void getByBookerId_differentStateFilters() {
        bookingService.add(booking1, user2.getId());
        bookingService.add(booking2, user2.getId());

        bookingService.approve(booking1.getId(), true, item1.getOwner().getId());

        StateFilter stateAll = StateFilter.ALL;
        int from = 0;
        int size = 10;

        List<BookingResponse> foundAllBookings =
                List.copyOf(bookingService.getByBookerId(stateAll, user2.getId(), from, size));

        assertEquals(2, foundAllBookings.size());

        StateFilter stateApproved = StateFilter.APPROVED;
        List<BookingResponse> foundApprovedBookings =
                List.copyOf(bookingService.getByBookerId(stateApproved, user2.getId(), from, size));

        assertEquals(1, foundApprovedBookings.size());
        assertEquals(BookingStatus.APPROVED, foundApprovedBookings.get(0).getStatus());
        assertEquals(booking1.getId(), foundApprovedBookings.get(0).getId());

        StateFilter stateWaiting = StateFilter.WAITING;
        List<BookingResponse> foundWaitingBookings =
                List.copyOf(bookingService.getByBookerId(stateWaiting, user2.getId(), from, size));

        assertEquals(1, foundWaitingBookings.size());
        assertEquals(BookingStatus.WAITING, foundWaitingBookings.get(0).getStatus());
        assertEquals(booking2.getId(), foundWaitingBookings.get(0).getId());

        StateFilter statePast = StateFilter.PAST;
        List<BookingResponse> foundPastBookings =
                List.copyOf(bookingService.getByBookerId(statePast, user2.getId(), from, size));

        assertEquals(0, foundPastBookings.size());
        assertEquals(Collections.emptyList(), foundPastBookings);

        StateFilter stateFuture = StateFilter.FUTURE;
        List<BookingResponse> foundFutureBookings =
                List.copyOf(bookingService.getByBookerId(stateFuture, user2.getId(), from, size));

        assertEquals(2, foundFutureBookings.size());

        StateFilter stateCurrent = StateFilter.CURRENT;
        List<BookingResponse> foundCurrentBookings =
                List.copyOf(bookingService.getByBookerId(stateCurrent, user2.getId(), from, size));

        assertEquals(0, foundCurrentBookings.size());
    }

    @Test
    void getByOwnerId_differentStateFilters() {
        bookingService.add(booking1, user2.getId());
        bookingService.add(booking2, user2.getId());

        bookingService.approve(booking1.getId(), true, item1.getOwner().getId());

        StateFilter stateAll = StateFilter.ALL;
        int from = 0;
        int size = 10;

        List<BookingResponse> foundAllBookings =
                List.copyOf(bookingService.getByOwnerId(stateAll, item1.getOwner().getId(), from, size));

        assertEquals(2, foundAllBookings.size());

        StateFilter stateApproved = StateFilter.APPROVED;
        List<BookingResponse> foundApprovedBookings =
                List.copyOf(bookingService.getByOwnerId(stateApproved, item1.getOwner().getId(), from, size));

        assertEquals(1, foundApprovedBookings.size());
        assertEquals(BookingStatus.APPROVED, foundApprovedBookings.get(0).getStatus());
        assertEquals(booking1.getId(), foundApprovedBookings.get(0).getId());

        StateFilter stateWaiting = StateFilter.WAITING;
        List<BookingResponse> foundWaitingBookings =
                List.copyOf(bookingService.getByOwnerId(stateWaiting, item1.getOwner().getId(), from, size));

        assertEquals(1, foundWaitingBookings.size());
        assertEquals(BookingStatus.WAITING, foundWaitingBookings.get(0).getStatus());
        assertEquals(booking2.getId(), foundWaitingBookings.get(0).getId());

        StateFilter statePast = StateFilter.PAST;
        List<BookingResponse> foundPastBookings =
                List.copyOf(bookingService.getByOwnerId(statePast, item1.getOwner().getId(), from, size));

        assertEquals(0, foundPastBookings.size());
        assertEquals(Collections.emptyList(), foundPastBookings);

        StateFilter stateFuture = StateFilter.FUTURE;
        List<BookingResponse> foundFutureBookings =
                List.copyOf(bookingService.getByOwnerId(stateFuture, item1.getOwner().getId(), from, size));

        assertEquals(2, foundFutureBookings.size());

        StateFilter stateCurrent = StateFilter.CURRENT;
        List<BookingResponse> foundCurrentBookings =
                List.copyOf(bookingService.getByOwnerId(stateCurrent, item1.getOwner().getId(), from, size));

        assertEquals(0, foundCurrentBookings.size());
    }

    @Test
    void getByBookerIdNotFound() {
        final long wrongUserId = 9999;

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getByBookerId(StateFilter.ALL, wrongUserId, 0, 10));
    }

    @Test
    void getByOwnerIdNotFound() {
        final long wrongUserId = 9999;

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getByOwnerId(StateFilter.ALL, wrongUserId, 0, 10));
    }

    @Test
    void addUserNotFound() {
        final long wrongUserId = 9999;
        assertThrows(EntityNotFoundException.class, () -> bookingService.add(booking1, wrongUserId));
    }

    @Test
    void approveBookingNotFound_thenUserNotFound() {
        final long wrongId = 9999;

        bookingService.add(booking1, user2.getId());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approve(wrongId, true, user1.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approve(booking1.getId(), true, wrongId));
    }
}
