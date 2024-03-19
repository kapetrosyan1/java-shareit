package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto findById(Long itemId, Long requesterId);

    List<ItemDtoWithBookings> findOwnersItems(Long userId);

    List<ItemDto> searchItems(String substring);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

    void delete(Long userId, Long itemId);
}