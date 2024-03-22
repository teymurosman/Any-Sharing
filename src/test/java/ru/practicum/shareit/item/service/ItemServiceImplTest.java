package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.exception.BookingException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    ItemServiceImpl itemService;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @Spy
    CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    Item item;

    User user;

    ItemResponse itemResponse;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("Joe");
        user.setEmail("joe@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setOwner(user);
        item.setAvailable(true);

        itemResponse = ItemResponse.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
    }

    @Test
    void addNormal() {
        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(item))
                .thenReturn(item);

        ItemResponse createdItem = itemService.add(user.getId(), item);

        assertEquals(user, item.getOwner());
        assertEquals(itemMapper.toItemResponse(item), createdItem);
        verify(itemRepository).save(item);
    }

    @Test
    void addWithNotExistingUser() {
        final long userId = 9999;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.add(userId, item));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getByItemIdNormal() {
        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        ItemResponse foundItem = itemService.getByItemId(user.getId(), item.getId());

        assertEquals(itemResponse, foundItem);
        verify(itemRepository).findById(item.getId());
    }

    @Test
    void getByWrongItemId() {
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.empty());

        final EntityNotFoundException e = assertThrows(EntityNotFoundException.class,
                () -> itemService.getByItemId(item.getId(), user.getId()));

        assertEquals("Вещь с id=" + item.getId() + " не найдена.", e.getMessage());
        verify(itemRepository).findById(item.getId());
    }

    @Test
    void getAllByOwnerId() {
        final int from = 0;
        final int size = 10;
        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(itemRepository.findByOwnerIdOrderByIdAsc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(item));

        Collection<ItemResponse> foundItems = itemService.getAllByOwnerId(user.getId(), from, size);

        assertEquals(1, foundItems.size());
        assertEquals(List.of(itemResponse), foundItems);
        verify(itemRepository).findByOwnerIdOrderByIdAsc(anyLong(), any(Pageable.class));
    }

    @Test
    void updateNormal() {
        Item itemForUpdate = new Item();
        itemForUpdate.setName("updated name");
        itemForUpdate.setDescription("updated description");
        itemForUpdate.setAvailable(false);
        itemResponse.setName("updated name");
        itemResponse.setDescription("updated description");
        itemResponse.setAvailable(false);

        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(item))
                .thenReturn(item);

        ItemResponse updatedItem = itemService.update(item.getId(), user.getId(), itemForUpdate);

        assertEquals(itemResponse, updatedItem);
        verify(itemRepository).save(any());
    }

    @Test
    void searchNormal() {
        String text = "еЛЬ";
        final int from = 0;
        final int size = 10;

        when(itemMapper.toItemResponse(item))
                .thenReturn(itemResponse);
        when(itemRepository.findAvailableBySubstring(anyString(), any(Pageable.class)))
                .thenReturn(List.of(item));

        Collection<ItemResponse> foundItems = itemService.search(text, from, size);

        assertEquals(1, foundItems.size());
        assertEquals(List.of(itemResponse), foundItems);
        verify(itemRepository).findAvailableBySubstring(anyString(), any(Pageable.class));
    }

    @Test
    void searchEmptySearchQuery() {
        String text = "";
        final int from = 0;
        final int size = 10;

        Collection<ItemResponse> foundItems = itemService.search(text, from, size);

        assertEquals(0, foundItems.size());
        verify(itemRepository, never()).findAvailableBySubstring(anyString(), any(Pageable.class));
    }

    @Test
    void addCommentNormal() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Комментарий на вещь");
        comment.setAuthor(user);

        when(commentMapper.toCommentResponse(any(Comment.class)))
                .thenReturn(CommentResponse.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .created(LocalDateTime.now())
                        .authorName(comment.getAuthor().getName())
                        .build());
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(new Booking()));
        when(commentRepository.save(comment))
                .thenReturn(comment);

        CommentResponse createdComment = itemService.addComment(item.getId(), comment, user.getId());

        assertEquals(user.getName(), createdComment.getAuthorName());
        assertNotNull(createdComment.getCreated());
        verify(commentRepository).save(comment);
    }

    @Test
    void addCommentWithoutHavingEndedBooking() {
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        assertThrows(BookingException.class, () -> itemService.addComment(item.getId(), new Comment(), user.getId()));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}