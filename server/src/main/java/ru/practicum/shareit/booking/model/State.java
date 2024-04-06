package ru.practicum.shareit.booking.model;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.exceptions.BadRequestException;

@Slf4j
public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static State stringToState(String state) {
        log.info("StateEnum: Конвертация строки {} в Enum State", state);
        try {
            return valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }
}
