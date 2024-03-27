package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;

import java.util.List;

public interface BookingService {
    BookingDtoWithItemAndUser addRequest(BookingRequestDto bookingRequestDto, Long userId);

    BookingDtoWithItemAndUser update(Long ownerId, Long bookingId, Boolean isApproved);

    BookingDtoWithItemAndUser findById(Long requesterId, Long bookingId);

    List<BookingDtoWithItemAndUser> findAllByUserAndState(Long userId, String state, int from, int size);

    List<BookingDtoWithItemAndUser> findAllByOwnerAndState(Long ownerId, String state, int from, int size);
}