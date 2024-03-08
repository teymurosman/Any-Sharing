package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingResponseForItemResponse;
import ru.practicum.shareit.booking.dto.BookingUpdateRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public interface BookingMapper {

    @Mapping(target = "item.id", source = "bookingCreateRequest.itemId")
    Booking toBooking(BookingCreateRequest bookingCreateRequest);

    Booking toBooking(BookingUpdateRequest bookingUpdateRequest);

    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingResponseForItemResponse toBookingResponseForItemResponse(Booking booking);
}
