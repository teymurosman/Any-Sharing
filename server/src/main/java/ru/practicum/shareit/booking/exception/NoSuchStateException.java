package ru.practicum.shareit.booking.exception;

public class NoSuchStateException extends RuntimeException {
    public NoSuchStateException(String message) {
        super(message);
    }
}
