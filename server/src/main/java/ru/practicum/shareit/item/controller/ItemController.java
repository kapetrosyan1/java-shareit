package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.Constant.USER_HEADER;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ItemResponseDto create(@RequestHeader(USER_HEADER) Long userId,
                                  @RequestBody ItemRequestDto itemRequestDto) {
        log.info("ItemController: обработка запроса от пользователя " + userId + " на добавление вещи + itemDto.toString()");
        return service.create(userId, itemRequestDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                                         @RequestBody CommentRequestDto commentRequestDto) {
        log.info("ItemController: обработка запроса от пользователя " + userId + " на добавление комментария " +
                commentRequestDto.toString() + " к вещи " + itemId);
        return service.addComment(userId, itemId, commentRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long itemId,
                                  @RequestBody ItemRequestDto itemRequestDto) {
        log.info("ItemController: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        return service.update(userId, itemId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDtoWithBookings findById(@PathVariable Long itemId, @RequestHeader(USER_HEADER) Long userId) {
        log.info("ItemController: обработка запроса на поиск вещи с id {}", itemId);
        return service.findById(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDtoWithBookings> findOwnersItems(@RequestHeader(USER_HEADER) Long userId,
                                                             @RequestParam(defaultValue = "0") int from,
                                                             @RequestParam(defaultValue = "10") int size) {
        log.info("ItemController: обработка запроса на поиск вещей пользователя с id {}", userId);
        return service.findOwnersItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestParam String text,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.info("ItemController: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        return service.searchItems(text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(USER_HEADER) Long userId,
                       @PathVariable Long itemId) {
        log.info("ItemController: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        service.delete(userId, itemId);
    }
}