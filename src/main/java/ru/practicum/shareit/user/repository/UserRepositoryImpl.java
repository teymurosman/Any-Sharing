package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserAlreadyExistsException;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long idCounter;

    @Override
    public User add(User user) {
        checkEmail(user);

        final long userId = ++idCounter;
        user.setId(userId);

        users.put(userId, user);
        emails.add(user.getEmail());

        return user;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User update(Long userId, User user) {
        User userToUpdate = users.get(userId);
        String newEmail = user.getEmail();
        String newName = user.getName();

        if (newEmail != null && !newEmail.isBlank() && newEmail.matches(".+[@].+[\\.].+")) {
            if (!userToUpdate.getEmail().equals(newEmail)) {
                checkEmail(user);
                emails.remove(userToUpdate.getEmail());
                emails.add(newEmail);
                userToUpdate.setEmail(newEmail);
            }
        }
        if (newName != null && !newName.isBlank()) {
            userToUpdate.setName(newName);
        }

        return userToUpdate;
    }

    @Override
    public void delete(Long userId) {
        User user = users.remove(userId);
        emails.remove(user.getEmail());
    }

    private void checkEmail(User user) {
        if (emails.contains(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с такой почтой уже сущетсвует.");
        }
    }
}
