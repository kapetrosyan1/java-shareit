package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingStorage storage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final Sort sortStartDesc = Sort.by(DESC, "startDate");

    @Override
    @Transactional
    public BookingDtoWithItemAndUser addRequest(BookingRequestDto bookingRequestDto, Long userId) {
        log.info("BookingService: получен запрос на добавление бронирования вещи {} от пользователя {}", bookingRequestDto,
                userId);
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart()) || bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new BadRequestException("Дата завершения бронирования должна быть позже даты начала");
        }
        Item item = itemStorage.findById(bookingRequestDto.getItemId()).orElseThrow(() -> new NotFoundException(
                String.format("Не удалось найти вещь с id %d", bookingRequestDto.getItemId())));
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        if (!item.getAvailable()) {
            throw new BadRequestException(String.format("Вещь c id %d недоступна для аренды", item.getId()));
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Собственник не может арендовать свою вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item, BookingStatus.WAITING);
        return BookingMapper.toBookingDtoWithItemAndUser(storage.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoWithItemAndUser update(Long ownerId, Long bookingId, Boolean isApproved) {
        log.info("BookingService: получен запрос на обновление бронирования с id {} от пользователя {}", bookingId,
                ownerId);
        Booking booking = storage.findById(bookingId).orElseThrow(() -> new NotFoundException(
                String.format("Бронирование с id %d не найдено", bookingId)));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException(String.format(
                    "Пользователь %d не является собственником бронируемой вещи", ownerId));
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BadRequestException(String.format("На бронирование %d уже был дан ответ", bookingId));
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDtoWithItemAndUser(storage.save(booking));
    }

    @Override
    public BookingDtoWithItemAndUser findById(Long requesterId, Long bookingId) {
        log.info("BookingService: получен запрос на поиск бронирования с id {} от пользователя {}", bookingId,
                requesterId);
        userStorage.findById(requesterId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", requesterId)));
        Booking booking = storage.findById(bookingId).orElseThrow(() -> new NotFoundException(
                String.format("Бронирование с id %d не найдено", bookingId)));

        if (!booking.getBooker().getId().equals(requesterId) && !booking.getItem().getOwner().getId().equals(requesterId)) {
            throw new NotFoundException(
                    String.format("Пользователь с id %d не является арендатором или собственником вещи", requesterId));
        }
        return BookingMapper.toBookingDtoWithItemAndUser(booking);
    }

    @Override
    public List<BookingDtoWithItemAndUser> findAllByUserAndState(Long userId, String stateString, int from, int size) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от пользователя {}",
                stateString, userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        State state = stringToState(stateString);
        PageRequest pageRequest = PageRequest.of((from / size), size, sortStartDesc);
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = storage.findAllByBookerId(userId, pageRequest).getContent();
                break;
            case CURRENT:
                bookings = storage.findAllByBookerIdAndStartDateBeforeAndEndDateAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), pageRequest).getContent();
                break;
            case PAST:
                bookings = storage.findAllByBookerIdAndEndDateBefore(userId, LocalDateTime.now(), pageRequest).getContent();
                break;
            case FUTURE:
                bookings = storage.findAllByBookerIdAndStartDateAfter(userId, LocalDateTime.now(), pageRequest).getContent();
                break;
            case WAITING:
                bookings = storage.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, pageRequest).getContent();
                break;
            case REJECTED:
                bookings = storage.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest).getContent();
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDtoWithItemAndUser).collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoWithItemAndUser> findAllByOwnerAndState(Long ownerId, String stateString, int from, int size) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от собственника {}",
                stateString, ownerId);
        List<Long> ids = itemStorage.findAllOwnersItemsIds(ownerId);
        if (ids.isEmpty()) {
            throw new NotFoundException(String.format("У пользователя с id %d нет вещей в собственности", ownerId));
        }
        List<Booking> bookings = new ArrayList<>();
        State state = stringToState(stateString);
        PageRequest pageRequest = PageRequest.of((from / size), size, sortStartDesc);
        switch (state) {
            case ALL:
                bookings = storage.findAllByItemOwnerId(ownerId, pageRequest).getContent();
                break;
            case CURRENT:
                bookings = storage.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
                        ownerId, LocalDateTime.now(), LocalDateTime.now(),
                        pageRequest).getContent();
                break;
            case PAST:
                bookings = storage.findAllByItemOwnerIdAndEndDateBefore(ownerId, LocalDateTime.now(),
                        pageRequest).getContent();
                break;
            case FUTURE:
                bookings = storage.findAllByItemOwnerIdAndStartDateAfter(ownerId, LocalDateTime.now(),
                        pageRequest).getContent();
                break;
            case WAITING:
                bookings = storage.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING,
                        pageRequest).getContent();
                break;
            case REJECTED:
                bookings = storage.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED,
                        pageRequest).getContent();
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDtoWithItemAndUser).collect(Collectors.toList());
    }

    private State stringToState(String stateString) {
        log.info("BookingService: конвертация строки {} в элемент перечисления", stateString);
        State state;
        try {
            state = State.valueOf(stateString);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", stateString));
        }
        return state;
    }
}