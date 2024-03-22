package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestResponseTest {

    private final JacksonTester<ItemRequestResponse> jacksonTester;

    @Test
    void convertFromResponseToJson() throws Exception {
        ItemRequestResponse itemRequestResponse = ItemRequestResponse.builder()
                .id(1L)
                .description("description of request")
                .created(LocalDateTime.of(2024, 2, 3, 4, 5, 6))
                .build();

        JsonContent<ItemRequestResponse> jsonContent = jacksonTester.write(itemRequestResponse);

        Assertions.assertThat(jsonContent).isEqualToJson("/itemRequest/ItemRequestResponse.json");
    }
}