package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingResponseTest {

    private final JacksonTester<BookingResponse> jacksonTester;

    @Test
    void convertFromBookingResponseToJson() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .name("userName")
                .email("user@email.com")
                .build();
        BookingResponseForItemResponse nextBooking = BookingResponseForItemResponse.builder()
                .id(2L)
                .bookerId(3L)
                .build();

        CommentResponse commentResponse = CommentResponse.builder()
                .id(4L)
                .text("comment for item")
                .authorName("userAuthor")
                .created(LocalDateTime.of(2024, 1, 2, 3, 4, 5))
                .build();

        ItemResponse itemResponse = ItemResponse.builder()
                .id(5L)
                .name("item name")
                .description("item description")
                .available(true)
                .nextBooking(nextBooking)
                .requestId(6L)
                .comments(Set.of(commentResponse))
                .build();

        BookingResponse bookingResponse = BookingResponse.builder()
                .id(7L)
                .item(itemResponse)
                .booker(userResponse)
                .start(LocalDateTime.of(2345, 1, 2, 3, 4, 5))
                .end(LocalDateTime.of(2356, 3, 4, 5, 6, 7))
                .status(BookingStatus.APPROVED)
                .build();

        JsonContent<BookingResponse> jsonContent = jacksonTester.write(bookingResponse);

        assertThat(jsonContent).isEqualToJson("/booking/BookingResponse.json");
    }
}