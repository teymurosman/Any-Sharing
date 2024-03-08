package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @PostMapping
    public ItemResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                            @Valid @RequestBody ItemCreateRequest itemCreateRequest) {
        return itemService.add(userId, itemMapper.toItem(itemCreateRequest));
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getByItemId(itemId, userId);
    }

    @GetMapping
    public Collection<ItemResponse> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllByOwnerId(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse update(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemUpdateRequest itemUpdateRequest) {
        return itemService.update(itemId, userId, itemMapper.toItem(itemUpdateRequest));
    }

    @GetMapping("/search")
    public Collection<ItemResponse> search(@RequestParam("text") String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse addComment(@PathVariable Long itemId,
                                           @Valid @RequestBody CommentCreateRequest commentCreateRequest,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(itemId, commentMapper.toComment(commentCreateRequest), userId);
    }
}
