package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestResponse add(Long userId, ItemRequest itemRequest);

    Collection<ItemRequestWithOffersResponse> getAllByUserId(Long userId);

    Collection<ItemRequestWithOffersResponse> getAll(Long userId, int from, int size);

    ItemRequestWithOffersResponse getById(Long userId, Long requestId);
}
