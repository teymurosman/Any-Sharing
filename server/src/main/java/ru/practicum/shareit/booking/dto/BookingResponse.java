package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {

    private Long id;

    private ItemResponse item;

    private UserResponse booker;

    private BookingStatus status;

    private LocalDateTime start;

    private LocalDateTime end;
}
