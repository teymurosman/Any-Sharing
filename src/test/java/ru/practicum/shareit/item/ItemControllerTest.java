package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemFromRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.CommentMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@Import({ItemMapperImpl.class, CommentMapperImpl.class})
class ItemControllerTest {

    public static final String PATH_ITEMS = "/items";
    public static final String HEADER_USER_ID = "X-Sharer-User-Id";
    public static final String PATH_BY_ID = "/items/{itemId}";

    @InjectMocks
    ItemController itemController;

    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemMapper itemMapper;

    @MockBean
    CommentMapper commentMapper;

    @Autowired
    ObjectMapper objectMapper;

    ItemFromRequest itemFromRequest;

    ItemResponse itemResponse;

    Item item;

    User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("Joe");
        user.setEmail("joe@mail.com");

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        itemFromRequest = ItemFromRequest.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
    }

    @Test
    void addNormal() throws Exception {
        when(itemMapper.toItem(itemFromRequest))
                .thenReturn(item);
        when(itemMapper.toItemResponse(any(Item.class)))
                .thenReturn(itemResponse);
        when(itemService.add(anyLong(), any(Item.class)))
                .thenReturn(itemResponse);

        mockMvc.perform(post(PATH_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 1)
                        .content(objectMapper.writeValueAsString(itemFromRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponse)))
                .andExpect(jsonPath("$.id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.name").value(itemResponse.getName()))
                .andExpect(jsonPath("$.description").value(itemResponse.getDescription()))
                .andExpect(jsonPath("$.available").value(itemResponse.getAvailable()));

        verify(itemService, times(1)).add(anyLong(), any(Item.class));
    }

    @Test
    void addWithEmptyName() throws Exception {
        itemFromRequest.setName("");

        mockMvc.perform(post(PATH_ITEMS)
                        .header(HEADER_USER_ID, 1)
                        .content(objectMapper.writeValueAsString(itemFromRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Название вещи не может быть пустым."));

        verify(itemService, never()).add(anyLong(), any(Item.class));
    }

    @Test
    void addWithoutHeaderXSharerUserId() throws Exception {
        mockMvc.perform(post(PATH_ITEMS)
                        .content(objectMapper.writeValueAsString(itemFromRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void addWithWrongUserId() throws Exception {
        final long userId = 9999;

        when(itemMapper.toItem(itemFromRequest))
                .thenReturn(item);
        when(itemService.add(userId, item))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(post(PATH_ITEMS)
                .header(HEADER_USER_ID, userId)
                .content(objectMapper.writeValueAsString(itemFromRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept("*/*"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addWithoutAvailable() throws Exception {
        itemFromRequest.setAvailable(null);

        mockMvc.perform(post(PATH_ITEMS)
                        .header(HEADER_USER_ID, 1)
                        .content(objectMapper.writeValueAsString(itemFromRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Статус вещи должен быть указан."));
    }

    @Test
    void addWithoutDescription() throws Exception {
        itemFromRequest.setDescription("");

        mockMvc.perform(post(PATH_ITEMS)
                        .header(HEADER_USER_ID, 1)
                        .content(objectMapper.writeValueAsString(itemFromRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.error").value("Описание вещи не может быть пустым."));
    }

    @Test
    void getItemByIdNormal() throws Exception {
        final long itemId = 1;
        final long userId = 1;

        when(itemMapper.toItemResponse(any(Item.class)))
                .thenReturn(itemResponse);
        when(itemService.getByItemId(itemId, userId))
                .thenReturn(itemResponse);

        mockMvc.perform(get(PATH_BY_ID, itemId)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponse)));

        verify(itemService, times(1)).getByItemId(itemId, userId);
    }

    @Test
    void getItemByWrongId() throws Exception {
        final long itemId = 9999;
        final long userId = 1;

        when(itemService.getByItemId(itemId, userId))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get(PATH_BY_ID, itemId)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getByItemId(itemId, userId);
    }

    @Test
    void getItemByIdWithoutHeaderXSharerUserId() throws Exception {
        final long itemId = 1;

        mockMvc.perform(get(PATH_BY_ID, itemId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllByOwnerIdNormal() throws Exception {
        final long userId = 1;
        final int from = 0;
        final int size = 10;

        List<ItemResponse> items = List.of(itemResponse);
        when(itemService.getAllByOwnerId(userId, from, size))
                .thenReturn(items);

        mockMvc.perform(get(PATH_ITEMS)
                .header(HEADER_USER_ID, userId)
                .param("form", String.valueOf(from))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));

        verify(itemService, times(1)).getAllByOwnerId(userId, from, size);
    }

    @Test
    void getAllByOwnerIdWithoutParamsNormal() throws Exception {
        final long userId = 1;

        List<ItemResponse> items = List.of(itemResponse);
        when(itemService.getAllByOwnerId(anyLong(), anyInt(), anyInt()))
                .thenReturn(items);

        mockMvc.perform(get(PATH_ITEMS)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));
    }

    @Test
    void updateNormal() throws Exception {
        final long itemId = 1;
        final long userId = 1;

        itemFromRequest = ItemFromRequest.builder()
                .name("Updated name")
                .description("Updated description")
                .available(false)
                .build();

        item.setName("Updated name");
        item.setDescription("Updated description");
        item.setAvailable(false);

        itemResponse = ItemResponse.builder()
                .name("Updated name")
                .description("Updated description")
                .available(false)
                .build();

        when(itemMapper.toItem(itemFromRequest))
                .thenReturn(item);
        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(itemService.update(anyLong(), anyLong(), any(Item.class)))
                .thenReturn(itemResponse);

        mockMvc.perform(patch(PATH_BY_ID, itemId)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemFromRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponse)));

        verify(itemService, times(1)).update(anyLong(), anyLong(), any(Item.class));
    }

    @Test
    void searchNormal() throws Exception {
        final long userId = 1;
        String text = "еЛЬ";
        final int from = 0;
        final int size = 10;

        List<ItemResponse> items = List.of(itemResponse);

        when(itemService.search(text, from, size))
                .thenReturn(items);

        mockMvc.perform(get(PATH_ITEMS + "/search")
                        .header(HEADER_USER_ID, userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));

        verify(itemService, times(1)).search(text, from, size);
    }

    @Test
    void addCommentNormal() throws Exception {
        final long itemId = 1;
        final long userId = 1;

        CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
                .text("Комментарий на дрель")
                .build();

        LocalDateTime created = LocalDateTime.of(2024, 3, 15, 12, 35, 40);
        CommentResponse commentResponse = CommentResponse.builder()
                .id(1L)
                .text("Комментарий на дрель")
                .authorName(user.getName())
                .created(created)
                .build();
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Комментарий на дрель");
        comment.setAuthor(user);
        comment.setCreated(created);

        when(commentMapper.toComment(commentCreateRequest))
                .thenReturn(comment);
        when(commentMapper.toCommentResponse(comment))
                .thenReturn(commentResponse);
        when(itemService.addComment(itemId, comment, userId))
                .thenReturn(commentResponse);

        mockMvc.perform(post(PATH_BY_ID + "/comment", itemId)
                .header(HEADER_USER_ID, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commentResponse)));
    }
}