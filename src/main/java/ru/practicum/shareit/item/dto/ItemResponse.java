package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingResponseForItemResponse;

import java.util.Set;

@Getter
@Setter
public class ItemResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingResponseForItemResponse lastBooking;

    private BookingResponseForItemResponse nextBooking;

    private Set<CommentResponse> comments;
}
