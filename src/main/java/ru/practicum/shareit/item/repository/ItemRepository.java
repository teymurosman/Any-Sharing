package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Item add(Long userId, Item item);

    Optional<Item> getItemById(Long itemId);

    Collection<Item> getAllByUserId(Long userId);

    Item update(Long itemId, Item item);

    Collection<Item> search(String text);
}
