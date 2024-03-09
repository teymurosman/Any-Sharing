package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingException;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ForbiddenAccessToEntityException;
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
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public ItemResponse add(Long userId, Item item) {
        log.debug("Добавление новой вещи({}) пользователем с id={}.", item.getName(), userId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
        item.setOwner(owner);

        Item item1 = itemRepository.save(item);
        return itemMapper.toItemResponse(item1);
    }

    @Override
    public ItemResponse getByItemId(Long itemId, Long userId) {
        log.debug("Получение вещи с id={}.", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с id=" + itemId + " не найдена."));

        if (!item.getOwner().getId().equals(userId)) {
            item.setLastBooking(null);
            item.setNextBooking(null);
        }
        return itemMapper.toItemResponse(item);
    }

    @Override
    public Collection<ItemResponse> getAllByOwnerId(Long userId) {
        log.debug("Получение списка вещей пользователя с id={}.", userId);

        return itemRepository.getByOwnerIdOrderByIdAsc(userId).stream()
                .map(itemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemResponse update(Long itemId, Long userId, Item item) {
        log.debug("Обновление данных вещи с id={} пользователем с id={}.", itemId, userId);

        Item itemToUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с id=" + itemId + " не найдена."));
        if (!itemToUpdate.getOwner().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Редактирование вещи доступно только её владельцу.");
        }

        if (item.getName() != null && !item.getName().isBlank()) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return itemMapper.toItemResponse(itemRepository.save(itemToUpdate));
    }

    @Override
    public Collection<ItemResponse> search(String searchQuery) {
        log.debug("Поиск вещей по запросу \"{}\".", searchQuery);

        if (searchQuery.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.findAvailableBySubstring(searchQuery).stream()
                .map(itemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentResponse addComment(Long itemId, Comment comment, Long userId) {
        log.debug("Добавление комментария к вещи с id={} от пользователя с id={}.", itemId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с id=" + itemId + " не найдена."));

        bookingRepository.findFirstByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now())
                .orElseThrow(() -> new BookingException("Оставлять комментарий возможно только после аренды вещи."));

        comment.setAuthor(author);
        comment.setItem(item);

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }
}
