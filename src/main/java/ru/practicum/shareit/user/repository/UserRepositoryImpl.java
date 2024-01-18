package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.exception.UserAlreadyExistsException;

import java.util.*;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long idCounter;

    @Override
    public User add(User user) {
        checkEmail(user.getEmail());

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
    public User update(Long userId, UserDto userDto) {
        User userToUpdate = users.get(userId);
        String newEmail = userDto.getEmail();
        String newName = userDto.getName();

        if (newEmail != null && !newEmail.isBlank() && newEmail.matches(".+[@].+[\\.].+")) {
            if (!userToUpdate.getEmail().equals(newEmail)) {
                checkEmail(newEmail);
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

    private void checkEmail(String email) {
        if (emails.contains(email)) {
            throw new UserAlreadyExistsException("Пользователь с такой почтой уже сущетсвует.");
        }
    }
}
