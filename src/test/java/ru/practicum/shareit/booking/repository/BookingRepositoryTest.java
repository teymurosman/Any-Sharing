package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.PageableFactory;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTest {

    public static final LocalDateTime CURRENT_TIME = LocalDateTime.now();

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    Booking booking1;

    Booking booking2;

    User owner1;

    User owner2;

    User booker1;

    User booker2;

    Item item1;

    Item item2;

    Sort sortStartDesc = Sort.by(Sort.Direction.DESC, "start");

    @BeforeEach
    void beforeEach() {
        owner1 = new User();
        owner1.setName("Joe");
        owner1.setEmail("joe@mail.com");

        owner2 = new User();
        owner2.setName("Bob");
        owner2.setEmail("bob@mail.com");

        booker1 = new User();
        booker1.setName("booker1");
        booker1.setEmail("booker1@mail.com");

        booker2 = new User();
        booker2.setName("booker2");
        booker2.setEmail("booker2@mail.com");

        userRepository.save(owner1);
        userRepository.save(owner2);
        userRepository.save(booker1);
        userRepository.save(booker2);

        item1 = new Item();
        item1.setName("item name qwerty");
        item1.setDescription("item description");
        item1.setAvailable(true);
        item1.setOwner(owner1);

        item2 = new Item();
        item2.setName("item2");
        item2.setDescription("description item2");
        item2.setAvailable(true);
        item2.setOwner(owner2);

        itemRepository.save(item1);
        itemRepository.save(item2);

        booking1 = new Booking();
        booking1.setItem(item1);
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setBooker(booker1);
        booking1.setStart(LocalDateTime.of(2100, 1, 2, 3, 4, 5));
        booking1.setEnd(LocalDateTime.of(2222, 2, 3, 4, 5, 6));

        booking2 = new Booking();
        booking2.setItem(item2);
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setBooker(booker2);
        booking2.setStart(LocalDateTime.of(2060, 5, 6, 7, 8, 9));
        booking2.setEnd(LocalDateTime.of(2085, 7, 8, 9, 10, 11));

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
    }

    @Test
    void findByBookerId() {
        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);

        List<Booking> foundBooker1Bookings = bookingRepository
                .findByBookerId(booker1.getId(), page);

        assertEquals(1, foundBooker1Bookings.size());
        assertEquals(booking1, foundBooker1Bookings.get(0));

        List<Booking> foundBooker2Bookings = bookingRepository
                .findByBookerId(booker2.getId(), page);

        assertEquals(1, foundBooker2Bookings.size());
        assertEquals(booking2, foundBooker2Bookings.get(0));

        long wrongBookerId = 9999;
        List<Booking> foundNoBookings = bookingRepository
                .findByBookerId(wrongBookerId, page);

        assertEquals(0, foundNoBookings.size());
    }

    @Test
    void findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual_stateCurrent() {
        booking2.setBooker(booking1.getBooker());
        booking1.setStart(booking1.getStart().minusYears(1000));

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);
        List<Booking> foundCurrent = bookingRepository
                .findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
                        booker1.getId(), CURRENT_TIME, CURRENT_TIME, page);

        assertEquals(1, foundCurrent.size());
        assertEquals(booking1, foundCurrent.get(0));
    }

    @Test
    void findByBookerIdAndEndLessThan_statePast() {
        booking2.setBooker(booking1.getBooker());
        bookingRepository.save(booking2);

        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);

        List<Booking> foundPast = bookingRepository.findByBookerIdAndEndLessThan(booker1.getId(), CURRENT_TIME, page);

        assertEquals(0, foundPast.size());
    }

    @Test
    void findByBookerId_pageAndSortTest() {
        booking2.setBooker(booking1.getBooker());
        booking1.setStart(booking2.getStart().plusMinutes(1));
        bookingRepository.save(booking2);

        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);
        List<Booking> foundSorted = bookingRepository.findByBookerId(booker1.getId(), page);

        assertEquals(2, foundSorted.size());
        assertEquals(booking1, foundSorted.get(0));
        assertEquals(booking2, foundSorted.get(1));

        Pageable pageFrom1Size1 = PageableFactory.getPageable(1, 1, sortStartDesc);
        List<Booking> foundFrom1Size1 = bookingRepository.findByBookerId(booker1.getId(), pageFrom1Size1);

        assertEquals(1, foundFrom1Size1.size());
        assertEquals(booking2, foundFrom1Size1.get(0));
    }

    @Test
    void findByBookerIdAndStatusIs() {
        booking2.setBooker(booking1.getBooker());
        bookingRepository.save(booking2);

        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);
        List<Booking> foundWaiting =
                bookingRepository.findByBookerIdAndStatusIs(booker1.getId(), BookingStatus.WAITING, page);

        assertEquals(2, foundWaiting.size());

        booking1.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking1);

        List<Booking> foundRejected =
                bookingRepository.findByBookerIdAndStatusIs(booker1.getId(), BookingStatus.REJECTED, page);

        assertEquals(1, foundRejected.size());
        assertEquals(booking1, foundRejected.get(0));

        List<Booking> foundApproved =
                bookingRepository.findByBookerIdAndStatusIs(booker1.getId(), BookingStatus.APPROVED, page);

        assertEquals(0, foundApproved.size());
    }

    @Test
    void findByItemOwnerId() {
        Pageable page = PageableFactory.getPageable(0, 10, sortStartDesc);
        List<Booking> foundOwner1 = bookingRepository.findByItemOwnerId(owner1.getId(), page);

        assertEquals(1, foundOwner1.size());
        assertEquals(booking1, foundOwner1.get(0));

        List<Booking> foundOwner2 = bookingRepository.findByItemOwnerId(owner2.getId(), page);

        assertEquals(1, foundOwner2.size());
        assertEquals(booking2, foundOwner2.get(0));

        long wrongOwnerId = 9999;

        List<Booking> foundWrongOwner = bookingRepository.findByItemOwnerId(wrongOwnerId, page);

        assertEquals(0, foundWrongOwner.size());
    }
}