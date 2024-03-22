package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Spy
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    User user1;

    User user2;

    UserResponse userResponse1;

    UserResponse userResponse2;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("Joe");
        user1.setEmail("joe@mail.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");

        userResponse1 = userMapper.toUserResponse(user1);

        userResponse2 = userMapper.toUserResponse(user2);
    }

    @Test
    void addNormal() {
        when(userRepository.save(user1))
                .thenReturn(user1);

        userService.add(user1);

        verify(userRepository).save(user1);
    }

    @Test
    void getByUserIdNormal() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        UserResponse foundUser = userService.getByUserId(user1.getId());

        assertEquals(userResponse1, foundUser);
        verify(userRepository).findById(user1.getId());
    }

    @Test
    void getByWrongUserId() {
        final long userId = 9999;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        final EntityNotFoundException e =
                assertThrows(EntityNotFoundException.class, () -> userService.getByUserId(userId));

        assertEquals("Пользователь с id=" + userId + " не найден.", e.getMessage());
    }

    @Test
    void getAllNormal() {
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll())
                .thenReturn(users);

        Collection<UserResponse> foundUsers = userService.getAll();

        assertEquals(2, foundUsers.size());
        assertEquals(List.of(userResponse1, userResponse2), foundUsers);
        verify(userRepository).findAll();
    }

    @Test
    void getAllEmpty() {
        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        Collection<UserResponse> foundUsers = userService.getAll();

        assertEquals(0, foundUsers.size());
    }

    @Test
    void updateNormal() {
        user1.setName("updated Joe");
        user1.setEmail("updatedjoe@mail.com");
        userResponse1.setName("updated Joe");
        userResponse1.setEmail("updatedjoe@mail.com");

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(userRepository.save(user1))
                .thenReturn(user1);

        UserResponse updatedUser = userService.update(user1.getId(), user1);

        assertEquals(userResponse1, updatedUser);
        verify(userRepository).save(user1);
    }

    @Test
    void updateEmptyNameAndIncorrectEmailShouldNotUpdate() {
        User userForUpdate = new User();
        userForUpdate.setName("");
        userForUpdate.setEmail("wrong.com@");

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(userRepository.save(user1))
                .thenReturn(user1);

        UserResponse updatedUser = userService.update(user1.getId(), userForUpdate);

        assertEquals(userResponse1, updatedUser);
        assertEquals("Joe", updatedUser.getName());
        assertEquals("joe@mail.com", updatedUser.getEmail());
    }

    @Test
    void deleteNormal() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        userService.delete(user1.getId());

        verify(userRepository).deleteById(user1.getId());
    }

    @Test
    void deleteWrongUserId() {
        final long userId = 9999;

        when(userRepository.findById(userId))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> userService.delete(userId));
        verify(userRepository, never()).deleteById(userId);
    }
}