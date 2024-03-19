package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static Booking toBooking(BookingDto bookingDto, User booker, Item item, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStartDate(bookingDto.getStart());
        booking.setEndDate(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStartDate());
        bookingDto.setEnd(booking.getEndDate());
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus());
        return bookingDto;
    }

    public static BookingDtoWithItemAndUser toBookingDtoWithItemAndUser(Booking booking) {
        BookingDtoWithItemAndUser bookingDtoWithItemAndUser = new BookingDtoWithItemAndUser();
        bookingDtoWithItemAndUser.setId(booking.getId());
        bookingDtoWithItemAndUser.setStart(booking.getStartDate());
        bookingDtoWithItemAndUser.setEnd(booking.getEndDate());
        bookingDtoWithItemAndUser.setItem(ItemMapper.toItemDto(booking.getItem()));
        bookingDtoWithItemAndUser.setBooker(UserMapper.toUserDto(booking.getBooker()));
        bookingDtoWithItemAndUser.setStatus(booking.getStatus());
        return bookingDtoWithItemAndUser;
    }
}