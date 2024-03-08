package ru.practicum.shareit.common;

public class ForbiddenAccessToEntityException extends RuntimeException {
    public ForbiddenAccessToEntityException(String message) {
        super(message);
    }
}
