package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(ItemRequestDto itemRequestDto, Long authorId);

    List<ItemRequestResponseDto> findAllByAuthorId(Long authorId);

    List<ItemRequestResponseDto> findAllRequests(Long userId, int from, int size);

    ItemRequestResponseDto findById(Long requesterId, Long itemRequestId);
}
