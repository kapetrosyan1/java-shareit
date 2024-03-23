package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public Booking toBooking(BookingRequestDto bookingRequestDto, User booker, Item item, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(bookingRequestDto.getId());
        booking.setStartDate(bookingRequestDto.getStart());
        booking.setEndDate(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(booking.getId());
        bookingResponseDto.setStart(booking.getStartDate());
        bookingResponseDto.setEnd(booking.getEndDate());
        bookingResponseDto.setItemId(booking.getItem().getId());
        bookingResponseDto.setBookerId(booking.getBooker().getId());
        bookingResponseDto.setStatus(booking.getStatus());
        return bookingResponseDto;
    }

    public BookingDtoWithItemAndUser toBookingDtoWithItemAndUser(Booking booking) {
        BookingDtoWithItemAndUser bookingDtoWithItemAndUser = new BookingDtoWithItemAndUser();
        bookingDtoWithItemAndUser.setId(booking.getId());
        bookingDtoWithItemAndUser.setStart(booking.getStartDate());
        bookingDtoWithItemAndUser.setEnd(booking.getEndDate());
        bookingDtoWithItemAndUser.setItem(ItemMapper.toItemResponseDto(booking.getItem()));
        bookingDtoWithItemAndUser.setBooker(UserMapper.toUserResponseDto(booking.getBooker()));
        bookingDtoWithItemAndUser.setStatus(booking.getStatus());
        return bookingDtoWithItemAndUser;
    }
}