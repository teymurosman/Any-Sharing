package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.PageableFactory;
import ru.practicum.shareit.item.dto.ItemForRequestResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Transactional
    @Override
    public ItemRequestResponse add(Long userId, ItemRequest itemRequest) {
        log.debug("Добавление нового запроса на вещь от пользователя с id={}", userId);

        User requester = getUser(userId);
        itemRequest.setRequester(requester);

        LocalDateTime created = LocalDateTime.now();
        itemRequest.setCreated(created);

        return itemRequestMapper.toItemRequestResponse(itemRequestRepository.save(itemRequest));
    }

    @Override
    public Collection<ItemRequestWithOffersResponse> getAllByUserId(Long userId) {
        log.debug("Получение списка запросов вещей с ответами пользователя с id={}", userId);

        getUser(userId);

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId);

        Map<Long, List<ItemForRequestResponse>> itemsByRequest = itemRepository.findByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(toList())).stream()
                .map(itemMapper::toItemForRequestResponse)
                .collect(groupingBy(ItemForRequestResponse::getRequestId, toList()));

        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestWithOffersResponse)
                .peek(itemRequest -> itemRequest.setItems(
                        itemsByRequest.getOrDefault(itemRequest.getId(), Collections.emptyList())))
                .collect(toList());
    }


    @Override
    public Collection<ItemRequestWithOffersResponse> getAll(Long userId, int from, int size) {
        log.debug("Получение всех запросов вещей.");

        getUser(userId);

        Pageable page = PageableFactory
                .getPageable(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdNot(userId, page);

        Map<Long, List<ItemForRequestResponse>> itemsByRequest = itemRepository.findByRequestIdIn(itemRequests.stream()
                        .map(ItemRequest::getId)
                        .collect(toList())).stream()
                .map(itemMapper::toItemForRequestResponse)
                .collect(groupingBy(ItemForRequestResponse::getRequestId, toList()));

        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestWithOffersResponse)
                .peek(itemRequest -> itemRequest.setItems(
                        itemsByRequest.getOrDefault(itemRequest.getId(), Collections.emptyList())))
                .collect(toList());
    }

    @Override
    public ItemRequestWithOffersResponse getById(Long userId, Long requestId) {
        log.debug("Получение запроса вещи с id={}.", requestId);

        getUser(userId);

        ItemRequestWithOffersResponse itemRequest = itemRequestMapper
                .toItemRequestWithOffersResponse(itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос вещи с id=" + requestId + " не найден.")));

        List<ItemForRequestResponse> items = itemRepository.findAllByRequestId(requestId).stream()
                        .map(itemMapper::toItemForRequestResponse)
                        .collect(toList());
        itemRequest.setItems(items);

        return itemRequest;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
    }
}
