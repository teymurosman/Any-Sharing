package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    User add(User user);

    Optional<User> getUserById(Long userId);

    Collection<User> getAll();

    User update(Long userId, User user);

    void delete(Long userId);
}
