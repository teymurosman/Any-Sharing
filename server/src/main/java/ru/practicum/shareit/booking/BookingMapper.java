package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.dto.BookingFromRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingResponseForItemResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public interface BookingMapper {

    @Mapping(target = "item.id", source = "bookingFromRequest.itemId")
    Booking toBooking(BookingFromRequest bookingFromRequest);

    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingResponseForItemResponse toBookingResponseForItemResponse(Booking booking);
}
