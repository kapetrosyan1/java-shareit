package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody @Valid ItemDto itemDto) {
        log.info("ItemController: обработка запроса от пользователя " + userId + " на добавление вещи + itemDto.toString()");
        return service.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                 @RequestBody @Valid CommentDto commentDto) {
        log.info("ItemController: обработка запроса от пользователя " + userId + " на добавление комментария " +
                commentDto.toString() + " к вещи " + itemId);
        return service.addComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("ItemController: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        return service.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("ItemController: обработка запроса на поиск вещи с id {}", itemId);
        return service.findById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoWithBookings> findOwnersItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("ItemController: обработка запроса на поиск вещей пользователя с id {}", userId);
        return service.findOwnersItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("ItemController: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        return service.searchItems(text);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @PathVariable Long itemId) {
        log.info("ItemController: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        service.delete(userId, itemId);
    }
}