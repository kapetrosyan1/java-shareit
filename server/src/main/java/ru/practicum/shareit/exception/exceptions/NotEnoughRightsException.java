package ru.practicum.shareit.exception.exceptions;

public class NotEnoughRightsException extends RuntimeException {
    public NotEnoughRightsException(String m) {
        super(m);
    }
}