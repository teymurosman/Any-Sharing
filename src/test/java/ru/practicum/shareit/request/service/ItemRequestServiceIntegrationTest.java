package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;

    ItemRequest itemRequest1;

    ItemRequest itemRequest2;
    User user1;

    User user2;

    Item item;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("Joe");
        user1.setEmail("joe@mail.com");

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");

        userService.add(user1);
        userService.add(user2);

        itemRequest1 = new ItemRequest();
        itemRequest1.setDescription("description for request 1");

        itemRequest2 = new ItemRequest();
        itemRequest2.setDescription("description of a second request");

        item = new Item();
        item.setName("item name");
        item.setDescription("item description");
        item.setAvailable(true);
        item.setRequestId(1L);
    }

    @Test
    void add_thenGetById_thenAddSecond_thenGetAll_thenGetAllByUserId() {
        itemRequestService.add(user1.getId(), itemRequest1);

        itemService.add(user2.getId(), item); // Add item owner=user2, request=itemRequest1


        ItemRequestWithOffersResponse foundItemRequest1 =
                itemRequestService.getById(user1.getId(), itemRequest1.getId());

        assertEquals(1L, foundItemRequest1.getId());
        assertEquals(itemRequest1.getDescription(), foundItemRequest1.getDescription());
        assertNotNull(foundItemRequest1.getCreated());
        assertEquals(1, foundItemRequest1.getItems().size());
        assertEquals(item.getId(), foundItemRequest1.getItems().get(0).getId());
        assertEquals(item.getName(), foundItemRequest1.getItems().get(0).getName());

        itemRequestService.add(user2.getId(), itemRequest2);

        int from = 0;
        int size = 10;
        List<ItemRequestWithOffersResponse> foundItemRequests =
                List.copyOf(itemRequestService.getAll(user1.getId(), from, size)); // Возвращается список не своих

        assertEquals(1, foundItemRequests.size());
        assertEquals(2L, foundItemRequests.get(0).getId());
        assertEquals(itemRequest2.getDescription(), foundItemRequests.get(0).getDescription());
        assertEquals(0, foundItemRequests.get(0).getItems().size());

        List<ItemRequestWithOffersResponse> foundItemRequestsByUserId =
                List.copyOf(itemRequestService.getAllByUserId(user1.getId()));

        assertEquals(1, foundItemRequestsByUserId.size());
        assertEquals(1L, foundItemRequestsByUserId.get(0).getId());
        assertEquals(1, foundItemRequestsByUserId.get(0).getItems().size());
    }

    @Test
    void addWithUserNotFound() {
        final long wrongUserId = 9999;

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.add(wrongUserId, itemRequest1));

        assertEquals(0, itemRequestService.getAll(user1.getId(), 0, 10).size());
        assertEquals(0, itemRequestService.getAllByUserId(user1.getId()).size());
    }
}
