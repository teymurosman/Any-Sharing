package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto add(User user) {
        log.debug("Добавление нового пользователя ({}).", user.getEmail());

        return UserMapper.toUserDto(userRepository.add(user));
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.debug("Получение пользователя с id={}.", userId);

        return UserMapper.toUserDto(userRepository.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + userId + " не найден.")));
    }

    @Override
    public Collection<UserDto> getAll() {
        log.debug("Получение списка всех пользователей.");

        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toSet());
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.debug("Обновление данных пользователя с id={}.", userId);

        getUserById(userId);

        return UserMapper.toUserDto(userRepository.update(userId, userDto));
    }

    @Override
    public void delete(Long userId) {
        log.debug("Удаление пользователя с id={}.", userId);
        getUserById(userId);

        userRepository.delete(userId);
    }
}
