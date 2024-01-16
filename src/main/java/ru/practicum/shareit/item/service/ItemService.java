package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    ItemDto add(Long userId, Item item);

    ItemDto getItemByById(Long itemId);

    Collection<ItemDto> getAllByUserId(Long userId);

    ItemDto update(Long itemId, Long userId, Item item);

    Collection<ItemDto> search(String text);
}
