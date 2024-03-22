package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	private long itemId;

	@NotNull(message = "Дата начала не может быть пустой.")
	@FutureOrPresent(message = "Дата начала не может быть в прошлом.")
	private LocalDateTime start;

	@NotNull(message = "Дата окончания не может быть пустой.")
	@Future(message = "Дата окончания должна быть в будущем.")
	private LocalDateTime end;
}
