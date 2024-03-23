package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
public class BookingDtoWithItemAndUser {
    private Long id;
    private ItemResponseDto item;
    private UserResponseDto booker;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}