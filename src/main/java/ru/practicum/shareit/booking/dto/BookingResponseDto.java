package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private Long itemId;
    private Long bookerId;
    @EqualsAndHashCode.Exclude
    private LocalDateTime start;
    @EqualsAndHashCode.Exclude
    private LocalDateTime end;
    private BookingStatus status;
}
