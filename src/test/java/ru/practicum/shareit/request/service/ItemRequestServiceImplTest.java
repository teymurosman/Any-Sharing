package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Spy
    ItemRequestMapper itemRequestMapper = Mappers.getMapper(ItemRequestMapper.class);

    @Spy
    ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    ItemRequest itemRequest;


    User user1;

    User user2;

    Item item;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("Joe");
        user1.setEmail("joe@mail.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");

        item = new Item();
        item.setId(1L);
        item.setOwner(user1);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Описание запроса вещи");
    }

    @Test
    void addNormal() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRequestRepository.save(itemRequest))
                .thenReturn(itemRequest);

        ItemRequestResponse foundItemRequest = itemRequestService.add(user2.getId(), itemRequest);

        assertEquals(itemRequestMapper.toItemRequestResponse(itemRequest), foundItemRequest);
        verify(itemRequestRepository).save(itemRequest);
    }

    @Test
    void addWithNotExistingRequester() {
        final long userId = 9999;
        when(userRepository.findById(userId))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.add(userId, itemRequest));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getAllByUserIdNormal() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRequestRepository.findAllByRequesterId(user2.getId()))
                .thenReturn(List.of(itemRequest));

        Collection<ItemRequestWithOffersResponse> foundItemRequests = itemRequestService.getAllByUserId(user2.getId());

        assertEquals(foundItemRequests.size(),1);
        verify(itemRequestRepository).findAllByRequesterId(user2.getId());
    }

    @Test
    void getAllNormal() {
        final int from = 0;
        final int size = 10;

        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));
        ItemRequestWithOffersResponse itemRequestWithOffersResponse =
                itemRequestMapper.toItemRequestWithOffersResponse(itemRequest);
        itemRequestWithOffersResponse.setItems(List.of(itemMapper.toItemForRequestResponse(item)));

        List<ItemRequestWithOffersResponse> foundItemRequests =
                List.copyOf(itemRequestService.getAll(user2.getId(), from, size));

        assertEquals(foundItemRequests.size(), 1);
        assertNotNull(foundItemRequests.get(0).getItems());
        verify(itemRequestRepository).findAllByRequesterIdNot(anyLong(), any(Pageable.class));
    }

    @Test
    void getByIdNormal() {
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findAllByRequestId(itemRequest.getId()))
                .thenReturn(List.of(item));
        ItemRequestWithOffersResponse itemRequestWithOffersResponse =
                itemRequestMapper.toItemRequestWithOffersResponse(itemRequest);
        itemRequestWithOffersResponse.setItems(List.of(itemMapper.toItemForRequestResponse(item)));

        ItemRequestWithOffersResponse foundItemRequest = itemRequestService.getById(user2.getId(), itemRequest.getId());

        assertEquals(1, foundItemRequest.getItems().size());
        assertEquals(item.getName(), foundItemRequest.getItems().get(0).getName());
        assertEquals(itemRequestWithOffersResponse, foundItemRequest);
        verify(itemRequestRepository).findById(itemRequest.getId());
    }

    @Test
    void getByWrongId() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getById(user2.getId(), itemRequest.getId()));
    }
}