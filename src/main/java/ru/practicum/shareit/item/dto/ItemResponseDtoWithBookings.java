package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemResponseDtoWithBookings extends ItemResponseDto {
    private BookingResponseDto lastBooking;
    private BookingResponseDto nextBooking;
}