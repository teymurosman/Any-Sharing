package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserResponse add(User user) {
        log.debug("Добавление нового пользователя ({}).", user.getEmail());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getByUserId(Long userId) {
        log.debug("Получение пользователя с id={}.", userId);

        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден.")));
    }

    @Override
    public Collection<UserResponse> getAll() {
        log.debug("Получение списка всех пользователей.");

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserResponse update(Long userId, User user) {
        log.debug("Обновление данных пользователя с id={}.", userId);

        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));

        if (user.getName() != null && !user.getName().isBlank()) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null && user.getEmail().matches(".+[@].+[\\.].+")) {
            userToUpdate.setEmail(user.getEmail());
        }

        return userMapper.toUserResponse(userRepository.save(userToUpdate));
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        log.debug("Удаление пользователя с id={}.", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));

        userRepository.deleteById(userId);
    }
}
