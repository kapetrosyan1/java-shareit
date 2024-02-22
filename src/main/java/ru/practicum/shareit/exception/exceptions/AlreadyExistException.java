package ru.practicum.shareit.exception.exceptions;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String m) {
        super(m);
    }
}