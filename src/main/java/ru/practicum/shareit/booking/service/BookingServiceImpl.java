package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.exception.BookingException;
import ru.practicum.shareit.booking.exception.NoSuchStateException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.StateFilter;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;


    @Transactional
    @Override
    public BookingResponse add(Booking booking, Long bookerId) {
        log.debug("Добавление нового бронирования для вещи с id={}.", booking.getItem().getId());

        if (!booking.getEnd().isAfter(booking.getStart())) {
            throw new BookingException("Дата начала бронирования должна быть раньше даты окончания.");
        }

        final long itemId = booking.getItem().getId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с id=" + itemId + " не найдена."));
        if (!item.getAvailable()) {
            throw new BookingException("Данная вещь недоступна для бронирования.");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + bookerId + " не найден."));
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ForbiddenAccessToEntityException("Невозможно бронировать собственную вещь.");
        }

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingResponse approve(Long bookingId, Boolean approved, Long ownerId) {
        log.debug("Обновление статуса бронирования вещи с id={}.", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование с id=" + bookingId + " не найдено."));

        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + ownerId + " не найден."));

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BookingException("Обновить статус вещи возможно только со статуса \"WAITING\".");
        }
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenAccessToEntityException("Обновление статуса бронирования" +
                    " возможно только владельцем вещи.");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse getByBookingId(Long bookingId, Long userId) {
        log.debug("Получение бронирования с id={}.", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование с id=" + bookingId + " не найдено."));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Получение данных о бронировании доступно" +
                    " только автору бронирования или владельцу вещи.");
        }

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public Collection<BookingResponse> getByBookerId(StateFilter state, Long bookerId, int from, int size) {
        log.debug("Получение списка бронирований пользователя с id={}.", bookerId);

        userRepository.findById(bookerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + bookerId + " не найден."));

        LocalDateTime currentTime = LocalDateTime.now();
        Sort sortStartDesc = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = getPageable(from, size, sortStartDesc);

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerId(bookerId, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
                        bookerId, currentTime, currentTime, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndEndLessThan(bookerId, currentTime, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartGreaterThan(bookerId, currentTime, page)
                        .stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case WAITING:
            case APPROVED:
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusIs(bookerId,
                                BookingStatus.valueOf(state.toString()), page)
                        .stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            default:
                throw new NoSuchStateException("Данный параметр поиска не поддерживается.");
        }
    }

    @Override
    public Collection<BookingResponse> getByOwnerId(StateFilter state, Long ownerId, int from, int size) {
        log.debug("Получение списка бронирований вещей по владельцу с id={}.", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + ownerId + " не найден."));

        LocalDateTime currentTime = LocalDateTime.now();
        Sort sortStartDesc = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = getPageable(from, size, sortStartDesc);

        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerId(ownerId, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(
                                ownerId, currentTime, currentTime, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndLessThan(ownerId, currentTime, page).stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartGreaterThan(ownerId, currentTime, page)
                        .stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            case WAITING:
            case APPROVED:
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusIs(ownerId,
                                BookingStatus.valueOf(state.toString()), page)
                        .stream()
                        .map(bookingMapper::toBookingResponse)
                        .collect(Collectors.toList());
            default:
                throw new NoSuchStateException("Данный параметр поиска не поддерживается.");
        }
    }

    private Pageable getPageable(int from, int size, Sort sort) {
        return PageRequest.of(from / size, size, sort);
    }
}