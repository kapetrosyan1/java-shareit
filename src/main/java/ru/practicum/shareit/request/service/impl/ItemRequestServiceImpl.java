package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage storage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    @Transactional
    public ItemRequestResponseDto create(ItemRequestDto itemRequestDto, Long authorId) {
        log.info("ItemRequestService: обработка запроса от пользователя {} на добавление ItemRequest {}",
                authorId, itemRequestDto.toString());
        User author = userStorage.findById(authorId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", authorId)));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, author);
        return ItemRequestMapper.toItemRequestResponseDto(storage.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> findAllByAuthorId(Long authorId) {
        log.info("ItemRequestService: обработка запроса от пользователя {} на поиск всех его ItemRequest", authorId);
        checkUser(authorId);
        List<ItemRequest> authorsRequests = storage.findAllByAuthorId(authorId, Sort.by(DESC, "created"));
        Map<ItemRequest, List<Item>> requestMap = itemStorage.findByItemRequestIn(authorsRequests, Sort.by(ASC, "id"))
                .stream()
                .collect(groupingBy(Item::getItemRequest, toList()));
        return authorsRequests.stream()
                .map(itemRequest -> setItemRequestItems(itemRequest, requestMap.get(itemRequest)))
                .collect(toList());
    }

    @Override
    public List<ItemRequestResponseDto> findAllRequests(Long userId, int from, int size) {
        checkUser(userId);
        PageRequest pageRequest = PageRequest.of((from / size), size, Sort.by(DESC, "created"));
        List<ItemRequest> responseDtoList = storage.findAllByAuthorIdNot(userId, pageRequest).getContent();
        Map<ItemRequest, List<Item>> itemsMap = itemStorage.findByItemRequestIn(responseDtoList, Sort.by(ASC, "id"))
                .stream()
                .collect(groupingBy(Item::getItemRequest, toList()));
        return responseDtoList.stream()
                .map(itemRequest -> setItemRequestItems(itemRequest, itemsMap.get(itemRequest)))
                .collect(toList());
    }

    @Override
    public ItemRequestResponseDto findById(Long requesterId, Long itemRequestId) {
        checkUser(requesterId);
        List<Item> itemList = itemStorage.findByItemRequestId(itemRequestId, Sort.by(ASC, "id"));
        ItemRequest itemRequest = storage.findById(itemRequestId).orElseThrow(() -> new NotFoundException(
                String.format("ItemRequest с id %d не найден", itemRequestId)
        ));
        return setItemRequestItems(itemRequest, itemList);
    }

    private ItemRequestResponseDto setItemRequestItems(ItemRequest itemRequest, List<Item> items) {
        List<ItemResponseDto> itemResponseDtoList = new ArrayList<>();
        if (items != null) {
            for (Item item : items) {
                itemResponseDtoList.add(ItemMapper.toItemResponseDto(item));
            }
        }
        ItemRequestResponseDto responseDto = ItemRequestMapper.toItemRequestResponseDto(itemRequest);
        responseDto.setItems(itemResponseDtoList);
        return responseDto;
    }

    private void checkUser(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
    }
}
