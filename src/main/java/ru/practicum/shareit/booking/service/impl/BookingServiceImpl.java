package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage storage;
    private final UserService userService;
    private final ItemStorage itemStorage;


    @Override
    @Transactional
    public BookingDtoWithItemAndUser addRequest(BookingDto bookingDto, Long userId) {
        log.info("BookingService: получен запрос на добавление бронирования вещи {} от пользователя {}", bookingDto,
                userId);
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new BadRequestException("Дата завершения бронирования должна быть позже даты начала");
        }
        Item item = itemStorage.findById(bookingDto.getItemId()).orElseThrow(() -> new NotFoundException(
                "Вещь с id " + bookingDto.getItemId() + " не найдена"));
        User user = UserMapper.toUser(userService.findById(userId));
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для аренды");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Собственник не может арендовать у себя вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, user, item, BookingStatus.WAITING);
        return BookingMapper.toBookingDtoWithItemAndUser(storage.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoWithItemAndUser update(Long ownerId, Long bookingId, Boolean isApproved) {
        log.info("BookingService: получен запрос на обновление бронирования с id {} от пользователя {}", bookingId,
                ownerId);
        Booking booking = storage.findById(bookingId).orElseThrow(() -> new NotFoundException(
                "Бронирование с id " + bookingId + " не найдено"));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Не найден запрос на бронирование вещи пользователя " + ownerId);
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BadRequestException("На бронирование " + bookingId + " уже был дан ответ");
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDtoWithItemAndUser(storage.save(booking));
    }

    @Override
    public BookingDtoWithItemAndUser findById(Long requesterId, Long bookingId) {
        log.info("BookingService: получен запрос на поиск бронирования с id {} от пользователя {}", bookingId,
                requesterId);
        userService.findById(requesterId);
        Booking booking = storage.findById(bookingId).orElseThrow(() -> new NotFoundException(
                "Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getBooker().getId().equals(requesterId) && !booking.getItem().getOwner().getId().equals(requesterId)) {
            throw new NotFoundException("Пользователь с id " + requesterId + " не является ни арендатором, ни " +
                    "арендодателем вещи");
        }
        return BookingMapper.toBookingDtoWithItemAndUser(booking);
    }

    @Override
    public List<BookingDtoWithItemAndUser> findAllByUserAndState(Long userId, String state) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от пользователя {}",
                state, userId);
        userService.findById(userId);
        switch (state) {
            case "ALL":
                return storage.findAllByBookerIdOrderByStartDateDesc(userId).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "CURRENT":
                return storage.findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(userId,
                                LocalDateTime.now(), LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "PAST":
                return storage.findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "FUTURE":
                return storage.findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "WAITING":
                return storage.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.WAITING).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "REJECTED":
                return storage.findAllByBookerIdAndStatusOrderByStartDateDesc(userId, BookingStatus.REJECTED).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDtoWithItemAndUser> findAllByOwnerAndState(Long ownerId, String state) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от собственника {}",
                state, ownerId);
        List<Long> ids = itemStorage.findAllOwnersItemsIds(ownerId);
        if (ids.isEmpty()) {
            throw new NotFoundException("У пользователя с id " + ownerId + " нет вещей в собственности");
        }
        switch (state) {
            case "ALL":
                return storage.findAllByItemOwnerIdOrderByStartDateDesc(ownerId).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "CURRENT":
                return storage.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                                ownerId, LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "PAST":
                return storage.findAllByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(ownerId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "FUTURE":
                return storage.findAllByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(ownerId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "WAITING":
                return storage.findAllByItemOwnerIdAndStatusOrderByStartDateDesc(ownerId, BookingStatus.WAITING).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case "REJECTED":
                return storage.findAllByItemOwnerIdAndStatusOrderByStartDateDesc(ownerId, BookingStatus.REJECTED).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}