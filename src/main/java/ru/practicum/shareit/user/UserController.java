package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateRequest;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserResponse add(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return userService.add(userMapper.toUser(userCreateRequest));
    }

    @GetMapping("/{userId}")
    public UserResponse getByUserId(@PathVariable Long userId) {
        return userService.getByUserId(userId);
    }

    @GetMapping
    public Collection<UserResponse> getAll() {
        return userService.getAll();
    }

    @PatchMapping("/{userId}")
    public UserResponse update(@PathVariable Long userId, @RequestBody UserUpdateRequest userUpdateRequest) {
        return userService.update(userId, userMapper.toUser(userUpdateRequest));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
