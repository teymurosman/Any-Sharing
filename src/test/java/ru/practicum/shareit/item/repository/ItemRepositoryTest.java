package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTest {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final TestEntityManager testEntityManager;

    Item item1;

    Item item2;

    User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("Joe");
        user.setEmail("joe@mail.com");

        userRepository.save(user);

        item1 = new Item();
        item1.setName("item name qwerty");
        item1.setDescription("item description");
        item1.setAvailable(true);

        item2 = new Item();
        item2.setName("second item");
        item2.setDescription("description second item qWERtY");
        item2.setAvailable(true);

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void findAvailableBySubstring_foundTwo() {
        String substring = "erT";
        int from = 0;
        int size = 10;

        List<Item> foundItems = itemRepository.findAvailableBySubstring(substring, PageRequest.of(from, size));

        assertEquals(2, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertTrue(foundItems.contains(item2));
    }

    @Test
    void findAvailableBySubstring_secondIsNotAvailable() {
        String substring = "erT";
        int from = 0;
        int size = 10;

        item2.setAvailable(false);
        itemRepository.save(item2);

        List<Item> foundItems = itemRepository.findAvailableBySubstring(substring, PageRequest.of(from, size));

        assertEquals(1, foundItems.size());
        assertTrue(foundItems.contains(item1));
        assertFalse(foundItems.contains(item2));
    }

    @Test
    void findAvailableBySubstring_noMatches() {
        String substring = "lpvsbsldchsnm";
        int from = 0;
        int size = 10;

        List<Item> foundItems = itemRepository.findAvailableBySubstring(substring, PageRequest.of(from, size));

        assertEquals(0, foundItems.size());
    }
}