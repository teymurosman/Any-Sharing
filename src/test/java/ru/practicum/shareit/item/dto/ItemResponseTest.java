package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseForItemResponse;

import java.time.LocalDateTime;
import java.util.Set;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemResponseTest {

    private final JacksonTester<ItemResponse> jacksonTester;

    @Test
    void convertFromItemResponseToJson() throws Exception {
        BookingResponseForItemResponse booking = BookingResponseForItemResponse.builder()
                .id(1L)
                .bookerId(2L)
                .build();

        CommentResponse comment = CommentResponse.builder()
                .id(3L)
                .text("comment for item")
                .created(LocalDateTime.of(2024, 1, 2, 3, 4, 5))
                .authorName("userName")
                .build();

        ItemResponse itemResponse = ItemResponse.builder()
                .id(4L)
                .name("item name")
                .description("description of item")
                .available(true)
                .nextBooking(booking)
                .requestId(5L)
                .comments(Set.of(comment))
                .build();

        JsonContent<ItemResponse> jsonContent = jacksonTester.write(itemResponse);

        Assertions.assertThat(jsonContent).isEqualToJson("/item/ItemResponse.json");
    }
}