package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentFromRequest;
import ru.practicum.shareit.item.dto.ItemFromRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @Valid @RequestBody ItemFromRequest itemFromRequest) {
        return itemClient.add(userId, itemFromRequest);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.getByItemId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "from", defaultValue = "0")
            @PositiveOrZero(message = "Параметр начала не может быть отрицательным") int from,
            @RequestParam(name = "size", defaultValue = "10")
            @Positive(message = "Параметр размера страницы должен быть больше 0") int size) {
        return itemClient.getAllByOwnerId(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") long userId,
                               @RequestBody ItemFromRequest itemFromRequest) {
        return itemClient.update(userId, itemId, itemFromRequest);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam("text") String text,
            @RequestParam(name = "from", defaultValue = "0")
            @PositiveOrZero(message = "Параметр начала не может быть отрицательным") int from,
            @RequestParam(name = "size", defaultValue = "10")
            @Positive(message = "Параметр размера страницы должен быть больше 0") int size) {
        return itemClient.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                      @Valid @RequestBody CommentFromRequest commentFromRequest,
                                      @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.addComment(userId, itemId, commentFromRequest);
    }
}
