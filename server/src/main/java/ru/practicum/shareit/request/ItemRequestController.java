package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestFromRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;

    @PostMapping
    public ItemRequestResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @RequestBody ItemRequestFromRequest itemRequestFromRequest) {
        return itemRequestService.add(userId, itemRequestMapper.toItemRequest(itemRequestFromRequest));
    }

    @GetMapping
    public Collection<ItemRequestWithOffersResponse> getAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllByUserId(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestWithOffersResponse> getAll(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithOffersResponse getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
}
