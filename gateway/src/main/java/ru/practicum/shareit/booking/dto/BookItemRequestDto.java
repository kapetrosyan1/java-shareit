package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	private long itemId;
	@NotNull(message = "Дата начала брони не может быть null")
	@FutureOrPresent(message = "Дата начала брони не может быть в прошлом")
	private LocalDateTime start;
	@NotNull(message = "Дата завершения брони не может быть null")
	@Future(message = "Дата завершения брони должна быть в будущем")
	private LocalDateTime end;
}
