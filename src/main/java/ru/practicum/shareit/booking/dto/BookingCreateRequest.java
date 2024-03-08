package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingCreateRequest {

    @NotNull(message = "Идентификатор вещи должен быть указан.")
    private Long itemId;

    @NotNull(message = "Дата начала брони должно быть указано.")
    @FutureOrPresent(message = "Дата начала брони не может быть в прошлом.")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания брони должна быть указана.")
    @Future(message = "Дата окончания брони должна быть в будущем.")
    private LocalDateTime end;
}
