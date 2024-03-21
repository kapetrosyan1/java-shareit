package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<BookingDtoWithItemAndUser> findAllByUserAndState(Long userId, String stateString) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от пользователя {}",
                stateString, userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        State state = stringToState(stateString);
        switch (state) {
            case ALL:
                return storage.findAllByBookerId(userId, Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case CURRENT:
                return storage.findAllByBookerIdAndStartDateBeforeAndEndDateAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), Sort.by(DESC, "startDate"))
                        .stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case PAST:
                return storage.findAllByBookerIdAndEndDateBefore(userId, LocalDateTime.now(),
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case FUTURE:
                return storage.findAllByBookerIdAndStartDateAfter(userId, LocalDateTime.now(),
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case WAITING:
                return storage.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING,
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case REJECTED:
                return storage.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED,
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingDtoWithItemAndUser> findAllByOwnerAndState(Long ownerId, String stateString) {
        log.info("BookingService: получен запрос на поиск всех бронирований с состоянием {} от собственника {}",
                stateString, ownerId);
        List<Long> ids = itemStorage.findAllOwnersItemsIds(ownerId);
        if (ids.isEmpty()) {
            throw new NotFoundException(String.format("У пользователя с id %d нет вещей в собственности", ownerId));
        }
        State state = stringToState(stateString);
        switch (state) {
            case ALL:
                return storage.findAllByItemOwnerId(ownerId, Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case CURRENT:
                return storage.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
                                ownerId, LocalDateTime.now(), LocalDateTime.now(),
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case PAST:
                return storage.findAllByItemOwnerIdAndEndDateBefore(ownerId, LocalDateTime.now(),
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case FUTURE:
                return storage.findAllByItemOwnerIdAndStartDateAfter(ownerId, LocalDateTime.now(),
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case WAITING:
                return storage.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING,
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            case REJECTED:
                return storage.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED,
                                Sort.by(DESC, "startDate")).stream()
                        .map(BookingMapper::toBookingDtoWithItemAndUser)
                        .collect(Collectors.toList());
            default:
                throw new BadRequestException(String.format("Unknown state: %s", stateString));
        }
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