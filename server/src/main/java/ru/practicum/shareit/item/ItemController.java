package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentFromRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemFromRequest;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @PostMapping
    public ItemResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                            @RequestBody ItemFromRequest itemFromRequest) {
        return itemService.add(userId, itemMapper.toItem(itemFromRequest));
    }

    @GetMapping("/{itemId}")
    public ItemResponse getByItemId(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getByItemId(itemId, userId);
    }

    @GetMapping
    public Collection<ItemResponse> getAllByOwnerId(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10")int size) {
        return itemService.getAllByOwnerId(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ItemResponse update(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemFromRequest itemFromRequest) {
        return itemService.update(itemId, userId, itemMapper.toItem(itemFromRequest));
    }

    @GetMapping("/search")
    public Collection<ItemResponse> search(
            @RequestParam("text") String text,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return itemService.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse addComment(@PathVariable Long itemId,
                                        @RequestBody CommentFromRequest commentFromRequest,
                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(itemId, commentMapper.toComment(commentFromRequest), userId);
    }
}
