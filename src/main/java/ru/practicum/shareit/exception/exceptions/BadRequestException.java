package ru.practicum.shareit.exception.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String m) {
        super(m);
    }
}