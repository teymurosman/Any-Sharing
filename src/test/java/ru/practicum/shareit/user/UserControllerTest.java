package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(UserMapperImpl.class)
class UserControllerTest {

    public static final String PATH_USERS = "/users";
    public static final String PATH_BY_ID = "/users/{userId}";

    @InjectMocks
    UserController userController;

    @MockBean
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    UserRequest userRequest;

    UserResponse userResponse;

    User user;

    @BeforeEach
    void beforeEach() {
        userRequest = UserRequest.builder()
                .name("Joe")
                .email("joe@mail.com")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .name("Joe")
                .email("joe@mail.com")
                .build();

        user = new User();
        user.setId(1L);
        user.setName("Joe");
        user.setEmail("joe@mail.com");
    }

    @Test
    void addNormal() throws Exception {
        when(userService.add(any(User.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post(PATH_USERS)
                            .content(objectMapper.writeValueAsString(userRequest))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userResponse)))
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));

        verify(userService, times(1)).add(any(User.class));
    }

    @Test
    void addWithEmptyName() throws Exception {
        userRequest.setName("");

        mockMvc.perform(post(PATH_USERS)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Имя пользователя не может быть пустым."));

        verify(userService, never()).add(any(User.class));
    }

    @Test
    void addWithWrongEmail() throws Exception {
        userRequest.setEmail("mail.com@");

        mockMvc.perform(post(PATH_USERS)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Неверный формат почты."));

        verify(userService, never()).add(any(User.class));
    }

    @Test
    void getByUserIdNormal() throws Exception {
        final long userId = 1;

        when(userService.getByUserId(userId))
                .thenReturn(userResponse);

        mockMvc.perform(get(PATH_BY_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userResponse)))
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));

        verify(userService, times(1)).getByUserId(userId);
    }

    @Test
    void getByWrongUserId() throws Exception {
        final long userId = 9999;

        when(userService.getByUserId(userId))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get(PATH_BY_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(EntityNotFoundException.class, result.getResolvedException()));

        verify(userService, times(1)).getByUserId(userId);
    }

    @Test
    void getAllNormal() throws Exception {
        final List<UserResponse> userResponseList = List.of(userResponse);

        when(userService.getAll())
                .thenReturn(userResponseList);

        mockMvc.perform(get(PATH_USERS))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userResponseList)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userResponse.getId()))
                .andExpect(jsonPath("$[0].name").value(userResponse.getName()))
                .andExpect(jsonPath("$[0].email").value(userResponse.getEmail()));

        verify(userService, times(1)).getAll();
    }

    @Test
    void getAllEmptyList() throws Exception {
        when(userService.getAll())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(PATH_USERS))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getAll();
    }

    @Test
    void updateNormal() throws Exception {
        userRequest.setName("updateName");
        userRequest.setEmail("updateEmail@mail.com");

        userResponse.setName(userRequest.getName());
        userResponse.setEmail(userRequest.getEmail());

        when(userService.update(anyLong(), any(User.class)))
                .thenReturn(userResponse);

        mockMvc.perform(patch(PATH_BY_ID, userResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));

        verify(userService, times(1)).update(anyLong(), any(User.class));
    }

    @Test
    void updateByWrongUserId() throws Exception {
        final long userId = 9999;
        userRequest.setName("updateName");
        userRequest.setEmail("updateEmail@mail.com");

        when(userService.update(userId, userMapper.toUser(userRequest)))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(patch(PATH_BY_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(EntityNotFoundException.class, result.getResolvedException()));

        verify(userService, times(1)).update(anyLong(), any(User.class));
    }

    @Test
    void deleteNormal() throws Exception {
        final long userId = 1;

        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete(PATH_BY_ID, userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    void deleteByWrongUserId() throws Exception {
        final long userId = 9999;

        doThrow(EntityNotFoundException.class).when(userService).delete(userId);

        mockMvc.perform(delete(PATH_BY_ID, userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).delete(anyLong());
    }
}