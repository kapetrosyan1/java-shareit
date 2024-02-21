package ru.practicum.shareit.item.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage storage;
    private final UserService service;

    public ItemServiceImpl(ItemStorage storage, UserService service) {
        this.storage = storage;
        this.service = service;
    }

    @Override
    public ItemDto create(int userId, ItemDto itemDto) {
        log.info("ItemService: обработка запроса от пользователя " + userId + " на добавление вещи " + itemDto.toString());
        service.findById(userId);
        return ItemMapper.toItemDto(storage.create(userId, itemDto));
    }

    @Override
    public ItemDto update(int userId, int itemId, ItemDto itemDto) {
        log.info("ItemService: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        service.findById(userId);
        return ItemMapper.toItemDto(storage.update(userId, itemId, itemDto));
    }

    @Override
    public ItemDto findById(int itemId) {
        log.info("ItemService: обработка запроса на поиск вещи с id {}", itemId);
        return ItemMapper.toItemDto(storage.findById(itemId));
    }

    @Override
    public List<ItemDto> findOwnersItems(int userId) {
        log.info("ItemService: обработка запроса на поиск вещей пользователя с id {}", userId);
        service.findById(userId);
        return storage.findOwnersItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("ItemService: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return storage.searchItems(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(int userId, int itemId) {
        log.info("ItemService: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        service.findById(userId);
        storage.delete(userId, itemId);
    }
}