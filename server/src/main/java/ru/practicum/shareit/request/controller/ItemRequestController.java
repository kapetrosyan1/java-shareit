package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.Constant.USER_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestResponseDto create(@RequestBody ItemRequestRequestDto itemRequestRequestDto,
                                         @RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemRequestController: обработка запроса на добавление ItemRequest от пользователя {}", userId);
        return service.create(itemRequestRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> findByAuthor(@RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemRequestController: обработка запроса на поиск всех ItemRequest, созданных пользователем {}", userId);
        return service.findAllByAuthorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> findAll(@RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemRequestController: обработка запроса на поиск всех ItemRequest от пользователя {}", userId);
        return service.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto findById(@PathVariable Long requestId, @RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemRequestController: обработка запроса на поиск ItemRequest с id {} от пользователя {}",
                requestId, userId);
        return service.findById(userId, requestId);
    }
}