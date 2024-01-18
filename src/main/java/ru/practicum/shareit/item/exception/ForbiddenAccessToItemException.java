package ru.practicum.shareit.item.exception;

public class ForbiddenAccessToItemException extends RuntimeException {
    public ForbiddenAccessToItemException(String message) {
        super(message);
    }
}
