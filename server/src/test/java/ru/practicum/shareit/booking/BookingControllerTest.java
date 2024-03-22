//package ru.practicum.shareit.booking;
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
//import ru.practicum.shareit.booking.dto.BookingFromRequest;
//import ru.practicum.shareit.booking.dto.BookingResponse;
//import ru.practicum.shareit.booking.exception.BookingException;
//import ru.practicum.shareit.booking.exception.NoSuchStateException;
//import ru.practicum.shareit.booking.model.Booking;
//import ru.practicum.shareit.booking.model.BookingStatus;
//import ru.practicum.shareit.booking.model.StateFilter;
//import ru.practicum.shareit.booking.service.BookingService;
//import ru.practicum.shareit.common.EntityNotFoundException;
//import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
//import ru.practicum.shareit.item.dto.ItemResponse;
//import ru.practicum.shareit.user.dto.UserResponse;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = BookingController.class)
//@Import(BookingMapperImpl.class)
//class BookingControllerTest {
//
//    public static final LocalDateTime START =
//            LocalDateTime.of(2030, 5, 22, 16, 41, 8);
//    public static final LocalDateTime END = START.plusDays(14);
//    public static final String PATH_BOOKINGS = "/bookings";
//    public static final String HEADER_USER_ID = "X-Sharer-User-Id";
//    public static final String PATH_BY_ID = "/bookings/{bookingId}";
//
//    @InjectMocks
//    BookingController bookingController;
//
//    @MockBean
//    BookingService bookingService;
//
//    @MockBean
//    BookingMapper bookingMapper;
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    BookingFromRequest bookingFromRequest;
//
//    BookingResponse bookingResponse;
//
//    Booking booking;
//
//    ItemResponse item;
//
//    UserResponse user;
//
//    @BeforeEach
//    void beforeEach() {
//        bookingFromRequest = BookingFromRequest.builder()
//                .itemId(1L)
//                .start(START)
//                .end(END)
//                .build();
//
//        item = ItemResponse.builder()
//                .id(1L)
//                .name("Дрель")
//                .description("Простая дрель")
//                .available(true)
//                .requestId(1L)
//                .build();
//
//        user = UserResponse.builder()
//                .id(1L)
//                .name("Joe")
//                .email("joe@mail.com")
//                .build();
//
//        bookingResponse = BookingResponse.builder()
//                .id(1L)
//                .item(item)
//                .booker(user)
//                .start(START)
//                .end(END)
//                .status(BookingStatus.WAITING)
//                .build();
//
//        booking = new Booking();
//        booking.setId(1L);
//        booking.setStart(START);
//        booking.setEnd(END);
//        booking.setStatus(BookingStatus.WAITING);
//    }
//
//    @Test
//    void addNormal() throws Exception {
//        final long userId = 1;
//
//        when(bookingMapper.toBooking(bookingFromRequest))
//                .thenReturn(booking);
//        when(bookingMapper.toBookingResponse(any(Booking.class)))
//                .thenReturn(bookingResponse);
//        when(bookingService.add(any(Booking.class), anyLong()))
//                .thenReturn(bookingResponse);
//
//        mockMvc.perform(post(PATH_BOOKINGS)
//                .header(HEADER_USER_ID, userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
//
//        verify(bookingService, times(1)).add(booking, userId);
//    }
//
//    @Test
//    void addEmptyItemId() throws Exception {
//        bookingFromRequest.setItemId(null);
//
//        mockMvc.perform(post(PATH_BOOKINGS)
//                .header(HEADER_USER_ID, user.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addStartInPast() throws Exception {
//        bookingFromRequest.setStart(START.minusYears(100));
//
//        mockMvc.perform(post(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, user.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addEndInPast() throws Exception {
//        bookingFromRequest.setEnd(END.minusYears(100));
//
//        mockMvc.perform(post(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, user.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void addWithoutHeaderXSharerUserId() throws Exception {
//        mockMvc.perform(post(PATH_BOOKINGS)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().is5xxServerError());
//    }
//
//    @Test
//    void addStartEqualsEnd() throws Exception {
//        bookingFromRequest.setEnd(bookingFromRequest.getStart());
//        booking.setEnd(booking.getStart());
//
//        when(bookingService.add(booking, user.getId()))
//                .thenThrow(BookingException.class);
//        when(bookingMapper.toBooking(bookingFromRequest))
//                .thenReturn(booking);
//
//        mockMvc.perform(post(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, user.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingFromRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void approveNormal() throws Exception {
//        final long bookingId = 1;
//        final long userId = 1;
//        final boolean approved = true;
//
//        bookingResponse.setStatus(BookingStatus.APPROVED);
//
//        when(bookingService.approve(bookingId, approved, userId))
//                .thenReturn(bookingResponse);
//
//        mockMvc.perform(patch(PATH_BY_ID, bookingId)
//                        .header(HEADER_USER_ID, userId)
//                        .param("approved", String.valueOf(approved)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
//
//        verify(bookingService, times(1)).approve(bookingId, approved, userId);
//    }
//
//    @Test
//    void approveWrongBookingId() throws Exception {
//        final long bookingId = 9999;
//        final long userId = 1;
//        final boolean approved = true;
//
//        when(bookingService.approve(bookingId, approved, userId))
//                .thenThrow(EntityNotFoundException.class);
//
//        mockMvc.perform(patch(PATH_BY_ID, bookingId)
//                        .header(HEADER_USER_ID, userId)
//                        .param("approved", String.valueOf(approved)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void approveWithoutApprovedParam() throws Exception {
//        final long bookingId = 1;
//        final long userId = 1;
//
//        mockMvc.perform(patch(PATH_BY_ID, bookingId)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getByBookingIdNormal() throws Exception {
//        final long bookingId = 1;
//        final long userId = 1;
//
//        when(bookingService.getByBookingId(bookingId, userId))
//                .thenReturn(bookingResponse);
//
//        mockMvc.perform(get(PATH_BY_ID, bookingId)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponse)));
//
//        verify(bookingService, times(1)).getByBookingId(bookingId, userId);
//    }
//
//    @Test
//    void getByBookingIdForbiddenAccess() throws Exception {
//        final long bookingId = 1;
//        final long userId = 9999;
//
//        when(bookingService.getByBookingId(bookingId, userId))
//                .thenThrow(ForbiddenAccessToEntityException.class);
//
//        mockMvc.perform(get(PATH_BY_ID, bookingId)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isNotFound())
//                .andExpect(result ->
//                        assertInstanceOf(ForbiddenAccessToEntityException.class, result.getResolvedException()));
//    }
//
//    @Test
//    void getByBookerIdNormal() throws Exception {
//        final long userId = 1;
//        final int from = 0;
//        final int size = 10;
//        final StateFilter state = StateFilter.ALL;
//
//        List<BookingResponse> bookings = List.of(bookingResponse);
//
//        when(bookingService.getByBookerId(state, userId, from, size))
//                .thenReturn(bookings);
//
//        mockMvc.perform(get(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, userId)
//                        .param("state", String.valueOf(state))
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookings)));
//
//        verify(bookingService, times(1)).getByBookerId(state, userId, from, size);
//    }
//
//    @Test
//    void getByBookerIdWithoutParamsNormal() throws Exception {
//        final long userId = 1;
//
//        List<BookingResponse> bookings = List.of(bookingResponse);
//
//        when(bookingService.getByBookerId(any(StateFilter.class), anyLong(), anyInt(), anyInt()))
//                .thenReturn(bookings);
//
//        mockMvc.perform(get(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, userId))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookings)));
//    }
//
//    @Test
//    void getByBookerIdWithoutHeaderXSharerUserId() throws Exception {
//        final int from = 0;
//        final int size = 10;
//        final StateFilter state = StateFilter.ALL;
//
//        List<BookingResponse> bookings = List.of(bookingResponse);
//
//        when(bookingService.getByBookerId(any(StateFilter.class), anyLong(), anyInt(), anyInt()))
//                .thenReturn(bookings);
//
//        mockMvc.perform(get(PATH_BOOKINGS)
//                        .param("state", String.valueOf(state))
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().is5xxServerError());
//    }
//
//    @Test
//    void getByBookerIdWrongStateFilter() throws Exception {
//        final long userId = 1;
//        final int from = 0;
//        final int size = 10;
//        String wrongStateFilter = "wrong filter";
//
//        when(bookingService.getByBookerId(any(StateFilter.class), anyLong(), anyInt(), anyInt()))
//                .thenThrow(NoSuchStateException.class);
//
//        mockMvc.perform(get(PATH_BOOKINGS)
//                        .header(HEADER_USER_ID, userId)
//                        .param("state", wrongStateFilter)
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void getByOwnerIdNormal() throws Exception {
//        final long userId = 1;
//        final int from = 0;
//        final int size = 10;
//        final StateFilter state = StateFilter.ALL;
//
//        List<BookingResponse> bookings = List.of(bookingResponse);
//
//        when(bookingService.getByOwnerId(state, userId, from, size))
//                .thenReturn(bookings);
//
//        mockMvc.perform(get(PATH_BOOKINGS + "/owner")
//                        .header(HEADER_USER_ID, userId)
//                        .param("state", String.valueOf(state))
//                        .param("from", String.valueOf(from))
//                        .param("size", String.valueOf(size)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookings)));
//    }
//}