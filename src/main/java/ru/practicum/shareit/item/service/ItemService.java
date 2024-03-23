package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(Long userId, ItemRequestDto itemRequestDto);

    ItemResponseDto update(Long userId, Long itemId, ItemRequestDto itemRequestDto);

    ItemResponseDtoWithBookings findById(Long itemId, Long requesterId);

    List<ItemResponseDtoWithBookings> findOwnersItems(Long userId);

    List<ItemResponseDto> searchItems(String substring);

    CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);

    void delete(Long userId, Long itemId);
}