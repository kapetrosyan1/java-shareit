package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(int userId, ItemDto itemDto);

    Item update(int userId, int itemId, ItemDto itemDto);

    Item findById(int itemId);

    List<Item> findOwnersItems(int userId);

    void delete(int userId, int itemId);

    List<Item> searchItems(String substring);
}