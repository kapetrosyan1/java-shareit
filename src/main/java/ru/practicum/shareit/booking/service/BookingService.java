package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;

import java.util.List;

public interface BookingService {
    BookingDtoWithItemAndUser addRequest(BookingDto bookingDto, Long userId);

    BookingDtoWithItemAndUser update(Long ownerId, Long bookingId, Boolean isApproved);

    BookingDtoWithItemAndUser findById(Long requesterId, Long bookingId);

    List<BookingDtoWithItemAndUser> findAllByUserAndState(Long userId, String state);

    List<BookingDtoWithItemAndUser> findAllByOwnerAndState(Long ownerId, String state);
}