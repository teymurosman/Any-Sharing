package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserResponse add(@RequestBody UserRequest userRequest) {
        return userService.add(userMapper.toUser(userRequest));
    }

    @GetMapping("/{userId}")
    public UserResponse getByUserId(@PathVariable Long userId) {
        return userService.getByUserId(userId);
    }

    @GetMapping
    public Collection<UserResponse> getAll() {
        return userService.getAll();
    }

    @PatchMapping("{userId}")
    public UserResponse update(@PathVariable long userId, @RequestBody UserRequest userRequest) {
        return userService.update(userId, userMapper.toUser(userRequest));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
