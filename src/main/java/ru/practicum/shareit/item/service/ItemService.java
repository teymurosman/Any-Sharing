package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    ItemResponse add(Long userId, Item item);

    ItemResponse getByItemId(Long itemId, Long userId);

    Collection<ItemResponse> getAllByOwnerId(Long userId);

    ItemResponse update(Long itemId, Long userId, Item item);

    Collection<ItemResponse> search(String text);

    CommentResponse addComment(Long itemId, Comment comment, Long userId);
}
