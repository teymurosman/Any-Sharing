package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.exception.ForbiddenAccessToItemException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto add(Long userId, Item item) {
        log.debug("Добавление новой вещи({}) пользователем с id={}.", item.getName(), userId);

        userService.getUserById(userId);

        return ItemMapper.toItemDto(itemRepository.add(userId, item));
    }

    @Override
    public ItemDto getItemByById(Long itemId) {
        log.debug("Получение вещи с id={}.", itemId);

        return ItemMapper.toItemDto(itemRepository.getItemById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id=" + itemId + " не найдена.")));
    }

    @Override
    public Collection<ItemDto> getAllByUserId(Long userId) {
        log.debug("Получение списка вещей пользователя с id={}.", userId);

        userService.getUserById(userId);

        return itemRepository.getAllByUserId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toSet());
    }

    @Override
    public ItemDto update(Long itemId, Long userId, Item item) {
        log.debug("Обновление данных вещи с id={} пользователем с id={}.", itemId, userId);

        userService.getUserById(userId);
        Item itemToUpdate = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id=" + itemId + " не найдена."));
        if (!itemToUpdate.getOwnerId().equals(userId)) {
            throw new ForbiddenAccessToItemException("Редактирование вещи доступно только её владельцу.");
        }

        return ItemMapper.toItemDto(itemRepository.update(itemId, item));
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.debug("Поиск вещей по запросу \"{}\".", text);

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toSet());
    }
}
