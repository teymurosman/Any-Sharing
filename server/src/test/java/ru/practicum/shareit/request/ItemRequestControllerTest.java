//package ru.practicum.shareit.request;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import ru.practicum.shareit.common.EntityNotFoundException;
//import ru.practicum.shareit.item.dto.ItemForRequestResponse;
//import ru.practicum.shareit.request.dto.ItemRequestFromRequest;
//import ru.practicum.shareit.request.dto.ItemRequestResponse;
//import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;
//import ru.practicum.shareit.request.service.ItemRequestService;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(controllers = ItemRequestController.class)
//@Import(ItemRequestMapperImpl.class)
//class ItemRequestControllerTest {
//
//    public static final LocalDateTime CREATED =
//            LocalDateTime.of(2024, 3, 15, 15, 20, 33);
//    public static final String PATH_REQUESTS = "/requests";
//    public static final String HEADER_USER_ID = "X-Sharer-User-Id";
//    @InjectMocks
//    ItemRequestController itemRequestController;
//
//    @MockBean
//    ItemRequestService itemRequestService;
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @MockBean
//    ItemRequestMapper itemRequestMapper;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    ItemRequestFromRequest itemRequestFromRequest;
//
//    ItemRequest itemRequest;
//
//    ItemRequestResponse itemRequestResponse;
//
//    ItemRequestWithOffersResponse itemRequestWithOffersResponse;
//
//    ItemForRequestResponse item;
//
//    @BeforeEach
//    void beforeEach() {
//        itemRequestFromRequest = ItemRequestFromRequest.builder()
//                .description("Нужна дрель")
//                .build();
//
//        itemRequest = new ItemRequest();
//        itemRequest.setId(1L);
//        itemRequest.setDescription("Нужна дрель");
//        itemRequest.setCreated(CREATED);
//
//        itemRequestResponse = ItemRequestResponse.builder()
//                .id(1L)
//                .description("Нужна дрель")
//                .created(CREATED)
//                .build();
//
//        item = ItemForRequestResponse.builder()
//                .id(1L)
//                .name("Дрель")
//                .description("Простая дрель")
//                .available(true)
//                .requestId(1L)
//                .build();
//
//        itemRequestWithOffersResponse = ItemRequestWithOffersResponse.builder()
//                .id(1L)
//                .description("Нужна дрель")
//                .created(CREATED)
//                .items(List.of(item))
//                .build();
//    }
//
//    @Test
//    void addNormal() throws Exception {
//        final long userId = 1;
//
//        when(itemRequestMapper.toItemRequest(itemRequestFromRequest))
//                .thenReturn(itemRequest);
//        when(itemRequestMapper.toItemRequestResponse(itemRequest))
//                .thenReturn(itemRequestResponse);
//        when(itemRequestService.add(userId, itemRequest))
//                .thenReturn(itemRequestResponse);
//
//        mockMvc.perform(post(PATH_REQUESTS)
//                .header(HEADER_USER_ID, userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(itemRequestFromRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(itemRequestResponse)));
//
//        verify(itemRequestService, times(1)).add(userId, itemRequest);
//    }
//
//    @Test
//    void addWithEmptyDescription() throws Exception {
//        final long userId = 1;
//
//        itemRequestFromRequest.setDescription("");
//
//        mockMvc.perform(post(PATH_REQUESTS)
//                .header(HEADER_USER_ID, userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(itemRequestFromRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(result ->
//                        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
//                .andExpect(jsonPath("$.error")
//                                .value("Описание запроса вещи не может быть пустым."));
//    }
//
//    @Test
//    void addWithoutHeaderXSharerUserId() throws Exception {
//        mockMvc.perform(post(PATH_REQUESTS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .contentType(objectMapper.writeValueAsString(itemRequestFromRequest)))
//                .andExpect(status().is5xxServerError());
//    }
//
//    @Test
//    void getAllByUserIdNormal() throws Exception {
//        final long userId = 1;
//
//        List<ItemRequestWithOffersResponse> itemRequests = List.of(itemRequestWithOffersResponse);
//        when(itemRequestMapper.toItemRequestWithOffersResponse(itemRequest))
//                .thenReturn(itemRequestWithOffersResponse);
//        when(itemRequestService.getAllByUserId(userId))
//                .thenReturn(itemRequests);
//
//        mockMvc.perform(get(PATH_REQUESTS)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(itemRequests)));
//    }
//
//    @Test
//    void getAllByUserIdWithoutHeaderXSharerUserId() throws Exception {
//        mockMvc.perform(get(PATH_REQUESTS))
//                .andExpect(status().is5xxServerError());
//    }
//
//    @Test
//    void getAllByWrongUserId() throws Exception {
//        final long userId = 9999;
//
//        when(itemRequestService.getAllByUserId(userId))
//                .thenThrow(EntityNotFoundException.class);
//
//        mockMvc.perform(get(PATH_REQUESTS)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void getAllNormal() throws Exception {
//        final long userId = 1;
//        final int from = 0;
//        final int size = 10;
//
//        List<ItemRequestWithOffersResponse> items = List.of(itemRequestWithOffersResponse);
//        when(itemRequestService.getAll(userId, from, size))
//                .thenReturn(items);
//
//        mockMvc.perform(get(PATH_REQUESTS + "/all")
//                        .header(HEADER_USER_ID, userId)
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(items)));
//    }
//
//    @Test
//    void getAllWithoutParamsNormal() throws Exception {
//        final long userId = 1;
//
//        List<ItemRequestWithOffersResponse> items = List.of(itemRequestWithOffersResponse);
//        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt()))
//                .thenReturn(items);
//
//        mockMvc.perform(get(PATH_REQUESTS + "/all")
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(items)));
//    }
//
//    @Test
//    void getAllWithWrongParams() throws Exception {
//        final long userId = 1;
//        final int from = -1;
//        final int size = 0;
//
//        mockMvc.perform(get(PATH_REQUESTS + "/all")
//                        .header(HEADER_USER_ID, userId)
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getByIdNormal() throws Exception {
//        final long userId = 1;
//        final long requestId = 1;
//
//        when(itemRequestService.getById(userId, requestId))
//                .thenReturn(itemRequestWithOffersResponse);
//
//        mockMvc.perform(get(PATH_REQUESTS + "/" + requestId)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(itemRequestWithOffersResponse)));
//    }
//
//    @Test
//    void getByWrongId() throws Exception {
//        final long userId = 1;
//        final long requestId = 9999;
//
//        when(itemRequestService.getById(userId, requestId))
//                .thenThrow(EntityNotFoundException.class);
//
//        mockMvc.perform(get(PATH_REQUESTS + "/" + requestId)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isNotFound());
//    }
//}