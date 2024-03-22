package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.exception.NoSuchStateException;

public enum StateFilter {

    ALL, CURRENT, PAST, FUTURE, WAITING, APPROVED, REJECTED;

    public static StateFilter parseString(String state) {
        try {
            return StateFilter.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new NoSuchStateException("Unknown state: " + state);
        }
    }

}
