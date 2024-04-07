package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.Constant.USER_HEADER;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingDtoWithItemAndUser addRequest(@RequestBody BookingRequestDto bookingRequestDto,
                                                @RequestHeader(USER_HEADER) Long userId) {
        log.info("BookingController: обработка запроса на добавление бронирования");
        return service.addRequest(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoWithItemAndUser update(@RequestHeader(USER_HEADER) Long ownerId, @PathVariable Long bookingId,
                                            @RequestParam Boolean approved) {
        log.info("BookingController: обработка запроса на обновление бронирования");
        return service.update(ownerId, bookingId, approved);
    }

    @GetMapping("{bookingId}")
    public BookingDtoWithItemAndUser findById(@RequestHeader(USER_HEADER) Long requesterId,
                                              @PathVariable Long bookingId) {
        log.info("BookingController: обработка запроса на поиск бронирования с id {}", bookingId);
        return service.findById(requesterId, bookingId);
    }

    @GetMapping
    public List<BookingDtoWithItemAndUser> findAllByUserAndState(@RequestHeader(USER_HEADER) Long requesterId,
                                                                 @RequestParam(defaultValue = "ALL") String state,
                                                                 @RequestParam(defaultValue = "0") int from,
                                                                 @RequestParam(defaultValue = "10") int size) {
        log.info("BookingController: обработка запроса от пользователя {} на поиск бронирований по состоянию {}",
                requesterId, state);
        return service.findAllByUserAndState(requesterId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoWithItemAndUser> findAllByOwnerAndState(@RequestHeader(USER_HEADER) Long ownerId,
                                                                  @RequestParam(defaultValue = "ALL") String state,
                                                                  @RequestParam(defaultValue = "0") int from,
                                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("BookingController: обработка запроса от пользователя {} на поиск всех бронирований его вещей с " +
                "состоянием {}", ownerId, state);
        return service.findAllByOwnerAndState(ownerId, state, from, size);
    }
}