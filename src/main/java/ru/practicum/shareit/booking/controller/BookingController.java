package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingDtoWithItemAndUser addRequest(@RequestBody @Valid BookingDto bookingDto,
                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("BookingController: обработка запроса на добавление бронирования");
        return service.addRequest(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoWithItemAndUser update(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId,
                                            @RequestParam Boolean approved) {
        log.info("BookingController: обработка запроса на обновление бронирования");
        return service.update(ownerId, bookingId, approved);
    }

    @GetMapping("{bookingId}")
    public BookingDtoWithItemAndUser findById(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                              @PathVariable Long bookingId) {
        log.info("BookingController: обработка запроса на поиск бронирования с id {}", bookingId);
        return service.findById(requesterId, bookingId);
    }

    @GetMapping
    public List<BookingDtoWithItemAndUser> findAllByUserAndState(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                                 @RequestParam(defaultValue = "ALL") String state) {
        log.info("BookingController: обработка запроса от пользователя {} на поиск бронирований по состоянию {}",
                requesterId, state);
        return service.findAllByUserAndState(requesterId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoWithItemAndUser> findAllByOwnerAndState(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("BookingController: обработка запроса от пользователя {} на поиск всех бронирований его вещей с " +
                "состоянием {}", ownerId, state);
        return service.findAllByOwnerAndState(ownerId, state);
    }
}