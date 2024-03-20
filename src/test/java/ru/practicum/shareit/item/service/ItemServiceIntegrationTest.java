package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    Item item1;

    Item item2;

    User user1;

    User user2;

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

        item1 = new Item();
        item1.setName("item name");
        item1.setDescription("item description");
        item1.setAvailable(true);

        item2 = new Item();
        item2.setName("second item");
        item2.setDescription("description second item");
        item2.setAvailable(true);
    }

    @Test
    void add_thenGetByItemId_thenAddSecond_thenGetAllByOwnerId_thenUpdateFirst_thenSearch_thenAddComment() {
        itemService.add(user1.getId(), item1);

        ItemResponse foundItem = itemService.getByItemId(item1.getId(), user2.getId());

        assertEquals(1L, foundItem.getId());
        assertEquals(item1.getName(), foundItem.getName());
        assertEquals(user1, item1.getOwner());

        itemService.add(user1.getId(), item2);

        int from = 0;
        int size = 10;
        List<ItemResponse> foundItems = List.copyOf(itemService.getAllByOwnerId(user1.getId(), from, size));

        assertEquals(2, foundItems.size());
        assertEquals(1L, foundItems.get(0).getId());
        assertEquals(2L, foundItems.get(1).getId());
        assertEquals(item2.getName(), foundItems.get(1).getName());

        item1.setName("updated name");
        item1.setDescription("updated description");
        item1.setAvailable(false);

        ItemResponse updatedItem = itemService.update(item1.getId(), user1.getId(), item1);

        assertEquals(1L, updatedItem.getId());
        assertEquals(item1.getName(), updatedItem.getName());
        assertEquals(item1.getDescription(), updatedItem.getDescription());
        assertEquals(item1.getAvailable(), updatedItem.getAvailable());

        String searchQuery = "deScR";

        List<ItemResponse> foundBySearch = List.copyOf(itemService.search(searchQuery, from, size)); // Available only

        assertEquals(1, foundBySearch.size());
        assertEquals(item2.getName(), foundBySearch.get(0).getName());
        assertEquals(item2.getDescription(), foundBySearch.get(0).getDescription());

        item1.setAvailable(true);
        itemService.update(item1.getId(), user1.getId(), item1);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(1, 1, 1, 1, 1));
        booking.setEnd(LocalDateTime.of(2, 2, 2, 2, 2));
        booking.setItem(item1);
        bookingService.add(booking, user2.getId());

        Comment comment = new Comment();
        comment.setText("comment for item1");
        comment.setCreated(LocalDateTime.now());

        CommentResponse createdComment = itemService.addComment(item1.getId(), comment, user2.getId());

        assertEquals(1L, createdComment.getId());
        assertEquals(comment.getText(), createdComment.getText());
        assertEquals(user2.getName(), createdComment.getAuthorName());
        assertEquals(comment.getCreated(), createdComment.getCreated());
    }

    @Test
    void updateNotFromOwner() {
        itemService.add(user1.getId(), item1);

        assertThrows(ForbiddenAccessToEntityException.class,
                () -> itemService.update(item1.getId(), user2.getId(), item2));
    }

    @Test
    void updateWithNullFields() {
        itemService.add(user1.getId(), item1);

        item2.setName(null);
        item2.setDescription(null);
        item2.setAvailable(null);

        ItemResponse updatedItem = itemService.update(item1.getId(), user1.getId(), item2);
        assertNotNull(updatedItem.getName());
        assertNotNull(updatedItem.getDescription());
        assertNotNull(updatedItem.getAvailable());
    }

    @Test
    void searchEmptyQuery() {
        itemService.add(user1.getId(), item1);
        itemService.add(user1.getId(), item2);

        String searchQuery = "";
        int from = 0;
        int size = 10;

        assertEquals(Collections.emptyList(), itemService.search(searchQuery, from, size));
    }

    @Test
    void addCommentUserNotFound_thenItemNotFound() {
        itemService.add(user1.getId(), item1);

        final long wrongId = 9999;

        Comment comment = new Comment();
        comment.setText("comment");

        assertThrows(EntityNotFoundException.class, () -> itemService.addComment(item1.getId(), comment, wrongId));

        assertThrows(EntityNotFoundException.class, () -> itemService.addComment(wrongId, comment, user2.getId()));
    }
}
