package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.Collection;

public interface UserService {

    UserResponse add(User user);

    UserResponse getByUserId(Long userId);

    Collection<UserResponse> getAll();

    UserResponse update(Long userId, User user);

    void delete(Long userId);
}
