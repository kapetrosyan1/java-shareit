package ru.practicum.shareit.exception.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String m) {
        super(m);
    }
}