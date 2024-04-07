package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(ItemRequestRequestDto itemRequestRequestDto, Long authorId);

    List<ItemRequestResponseDto> findAllByAuthorId(Long authorId);

    List<ItemRequestResponseDto> findAllRequests(Long userId, int from, int size);

    ItemRequestResponseDto findById(Long userId, Long itemRequestId);
}
