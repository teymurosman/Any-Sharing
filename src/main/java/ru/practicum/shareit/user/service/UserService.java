package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto add(User user);

    UserDto getUserById(Long userId);

    Collection<UserDto> getAll();

    UserDto update(Long userId, User user);

    void delete(Long userId);
}
