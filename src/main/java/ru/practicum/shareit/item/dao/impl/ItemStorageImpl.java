package ru.practicum.shareit.item.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemStorageImpl implements ItemStorage {
    private final HashMap<Integer, Item> storage = new HashMap<>();
    private final HashMap<Integer, List<Integer>> ownerItems = new HashMap<>();
    private int id = 1;

    @Override
    public Item create(int userId, ItemDto itemDto) {
        log.info("ItemStorage: обработка запроса на добавление вещи {} от пользователя {}", itemDto.toString(), userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setId(id);
        item.setOwner(userId);
        storage.put(id, item);
        if (ownerItems.containsKey(userId)) {
            ownerItems.get(userId).add(id);
            id++;
            return item;
        }
        ownerItems.put(userId, List.of(item.getId()));
        id++;
        return item;
    }

    @Override
    public Item update(int userId, int itemId, ItemDto itemDto) {
        log.info("ItemStorage: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        if (!storage.containsKey(itemId) || storage.get(itemId).getOwner() != userId) {
            throw new NotFoundException("Вещь с id " + itemId +
                    " не найдена или не является собственностью пользователя с id " + userId);
        }
        Item item = storage.get(itemId);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return item;
    }

    @Override
    public Item findById(int itemId) {
        log.info("ItemStorage: обработка запроса на поиск вещи с id {}", itemId);
        if (!storage.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
        return storage.get(itemId);
    }

    @Override
    public List<Item> findOwnersItems(int userId) {
        log.info("ItemStorage: обработка запроса на поиск вещей пользователя с id {}", userId);
        List<Item> itemList = new ArrayList<>();
        for (int itemId : ownerItems.get(userId)) {
            itemList.add(storage.get(itemId));
        }
        return itemList;
    }

    @Override
    public List<Item> searchItems(String text) {
        log.info("ItemStorage: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        return storage.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> (item.getName().toLowerCase().contains(text)) ||
                        item.getDescription().toLowerCase().contains(text))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(int userId, int itemId) {
        log.info("ItemStorage: обработка запроса на удаление вещи с id {} от пользователя {}",itemId, userId);
        if (!storage.containsKey(itemId) || storage.get(itemId).getOwner() != userId) {
            throw new NotFoundException("Вещь с id " + itemId + " и с владельцем с id " + userId + " не найдена");
        }
        storage.remove(itemId);
        ownerItems.get(userId).remove(itemId);
    }
}