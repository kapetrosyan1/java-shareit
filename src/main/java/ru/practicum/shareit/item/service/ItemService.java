package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(int userId, ItemDto itemDto);

    ItemDto update(int userId, int itemId, ItemDto itemDto);

    ItemDto findById(int itemId);

    List<ItemDto> findOwnersItems(int userId);

    List<ItemDto> searchItems(String substring);

    void delete(int userId, int itemId);
}