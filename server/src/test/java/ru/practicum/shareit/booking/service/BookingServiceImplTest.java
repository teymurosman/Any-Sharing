package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.exception.BookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    BookingServiceImpl bookingService;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    Booking booking;

    BookingResponse bookingResponse;

    Item item;

    User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("Joe");
        user.setEmail("joe@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setOwner(user);
        item.setAvailable(true);

        Item itemToSet = new Item();
        itemToSet.setId(1L);
        itemToSet.setOwner(user);
        booking = new Booking();
        booking.setId(1L);
        booking.setItem(itemToSet);
        booking.setStart(LocalDateTime.of(2030, 5, 21, 12, 0, 0));
        booking.setEnd(LocalDateTime.of(2033, 11, 8, 22, 45, 32));

        ItemResponse itemResponse = ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        bookingResponse = BookingResponse.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(userMapper.toUserResponse(user))
                .item(itemResponse)
                .status(BookingStatus.WAITING)
                .build();

    }

    @Test
    void addNormal() {
        final long bookerId = 2;

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user));
        when(bookingRepository.save(booking))
                .thenReturn(booking);

        BookingResponse createdBooking = bookingService.add(booking, bookerId);

        assertNotNull(createdBooking.getBooker());
        assertNotNull(createdBooking.getItem());
        assertEquals(BookingStatus.WAITING, createdBooking.getStatus());
        assertEquals(bookingResponse, createdBooking);
        verify(bookingRepository).save(booking);
    }

    @Test
    void addStartAfterEnd() {
        booking.setStart(booking.getEnd().plusDays(1));

        assertThrows(BookingException.class, () -> bookingService.add(booking, user.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void addItemNotAvailable() {
        item.setAvailable(false);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        assertThrows(BookingException.class, () -> bookingService.add(booking, user.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void addBookerSameAsOwner() {
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertThrows(ForbiddenAccessToEntityException.class, () -> bookingService.add(booking, user.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveSetApprovedNormal() {
        booking.setStatus(BookingStatus.WAITING);
        bookingResponse.setStatus(BookingStatus.APPROVED);

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(bookingRepository.save(booking))
                .thenReturn(booking);
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        BookingResponse approvedBooking = bookingService.approve(booking.getId(), true, user.getId());

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
        assertEquals(bookingResponse, approvedBooking);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveSetRejectedNormal() {
        booking.setStatus(BookingStatus.WAITING);
        bookingResponse.setStatus(BookingStatus.REJECTED);

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(bookingRepository.save(booking))
                .thenReturn(booking);
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        BookingResponse approvedBooking = bookingService.approve(booking.getId(), false, user.getId());

        assertEquals(BookingStatus.REJECTED, approvedBooking.getStatus());
        assertEquals(bookingResponse, approvedBooking);
    }

    @Test
    void approveNotFromOwner() {
        final long wrongId = 9999;
        booking.setStatus(BookingStatus.WAITING);

        when(userRepository.findById(wrongId))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(ForbiddenAccessToEntityException.class,
                () -> bookingService.approve(booking.getId(), true, wrongId));
    }

    @Test
    void approveNotFromWaiting() {
        booking.setStatus(BookingStatus.APPROVED);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(BookingException.class,
                () -> bookingService.approve(booking.getId(), false, user.getId()));
    }

    @Test
    void getByBookingIdNormal() {
        User booker = new User();
        booker.setId(2L);
        booking.setBooker(booker);

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingResponse foundBooking = bookingService.getByBookingId(booking.getId(), user.getId());

        assertEquals(bookingResponse, foundBooking);
        verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void getByWrongBookingId() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.getByBookingId(booking.getId(), user.getId()));
    }

    @Test
    void getByBookingIdFromWrongUser() {
        final long wrongId = 9999;

        User booker = new User();
        booker.setId(2L);
        booking.setBooker(booker);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(ForbiddenAccessToEntityException.class,
                () -> bookingService.getByBookingId(booking.getId(), wrongId));
    }

    @Test
    void getByBookerIdStateAllNormal() {
        final StateFilter state = StateFilter.ALL;
        final int from = 0;
        final int size = 10;
        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        Collection<BookingResponse> foundBookings = bookingService.getByBookerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        any(Pageable.class));
    }

    @Test
    void getByBookerIdStatePastNormal() {
        final StateFilter state = StateFilter.PAST;
        final int from = 0;
        final int size = 10;

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndEndLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        Collection<BookingResponse> foundBookings = bookingService.getByBookerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository)
                .findByBookerIdAndEndLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
    }

    @Test
    void getByBookerIdStateApproved() {
        final StateFilter state = StateFilter.APPROVED;
        final int from = 0;
        final int size = 10;
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "start"));

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusIs(user.getId(), BookingStatus.APPROVED, page))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);

        Collection<BookingResponse> foundBookings = bookingService.getByBookerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository).findByBookerIdAndStatusIs(user.getId(), BookingStatus.APPROVED, page);
        verify(bookingRepository, never()).findByBookerIdAndStartGreaterThan(anyLong(), any(), any());
    }

    @Test
    void getByOwnerIdStateAllNormal() {
        final StateFilter state = StateFilter.ALL;
        final int from = 0;
        final int size = 10;
        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        Collection<BookingResponse> foundBookings = bookingService.getByOwnerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository).findByItemOwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        any(Pageable.class));
    }

    @Test
    void getByOwnerIdStatePastNormal() {
        final StateFilter state = StateFilter.PAST;
        final int from = 0;
        final int size = 10;

        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndEndLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        Collection<BookingResponse> foundBookings = bookingService.getByOwnerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository)
                .findByItemOwnerIdAndEndLessThan(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    void getByOwnerIdStateApproved() {
        final StateFilter state = StateFilter.APPROVED;
        final int from = 0;
        final int size = 10;
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "start"));

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdAndStatusIs(user.getId(), BookingStatus.APPROVED, page))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponse(booking))
                .thenReturn(bookingResponse);

        Collection<BookingResponse> foundBookings = bookingService.getByOwnerId(state, user.getId(), from, size);

        assertEquals(1, foundBookings.size());
        assertTrue(foundBookings.contains(bookingResponse));
        verify(bookingRepository).findByItemOwnerIdAndStatusIs(user.getId(), BookingStatus.APPROVED, page);
        verify(bookingRepository, never()).findByItemOwnerIdAndStartGreaterThan(anyLong(), any(), any());
    }
}