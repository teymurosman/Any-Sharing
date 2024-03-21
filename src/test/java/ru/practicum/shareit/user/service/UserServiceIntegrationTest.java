package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceIntegrationTest {

    private final UserService userService;

    User user1;

    User user2;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("Joe");
        user1.setEmail("joe@mail.com");

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");
    }

    @Test
    void addA_thenGetById_thenAddSecondUser_thenGetAll_thenUpdateFirstUser_thenDeleteSecond_thenGetNotFound() {
        userService.add(user1);

        UserResponse returnedUser1 = userService.getByUserId(1L);

        assertNotNull(returnedUser1);
        assertEquals(1, returnedUser1.getId());
        assertEquals("Joe", returnedUser1.getName());
        assertEquals("joe@mail.com", returnedUser1.getEmail());

        userService.add(user2);

        List<UserResponse> users = List.copyOf(userService.getAll());

        assertEquals(2, users.size());
        assertEquals(1, users.get(0).getId());
        assertEquals(2, users.get(1).getId());
        assertEquals("Bob", users.get(1).getName());

        user1.setName("Updated Name");
        user1.setEmail("updatedEmail@mail.com");

        UserResponse updatedUser1 = userService.update(1L, user1);

        assertEquals(1L, updatedUser1.getId());
        assertEquals(user1.getName(), updatedUser1.getName());
        assertEquals(user1.getEmail(), updatedUser1.getEmail());

        userService.delete(2L);

        assertThrows(EntityNotFoundException.class, () -> userService.getByUserId(2L));
    }

    @Test
    void addDuplicateEmail() {
        userService.add(user1);

        user2.setEmail(user1.getEmail());

        assertThrows(DataIntegrityViolationException.class, () -> userService.add(user2));
        assertThrows(EntityNotFoundException.class, () -> userService.getByUserId(2L));
    }

    @Test
    void updateEmptyNameAndIncorrectEmail_shouldNotUpdate() {
        userService.add(user1);

        user2.setName("");
        user2.setEmail("wrong.com@");

        userService.update(user1.getId(), user2);
        UserResponse updatedUser = userService.getByUserId(user1.getId());

        assertEquals(user1.getName(), updatedUser.getName());
        assertEquals(user1.getEmail(), updatedUser.getEmail());
    }
}
