package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    public ItemDto create(@Valid @RequestHeader("X-Sharer-User-Id") int userId,
                          @RequestBody @Valid ItemDto itemDto) {
        log.info("ItemController: обработка запроса от пользователя " + userId + " на добавление вещи + itemDto.toString()");
        return service.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") int userId,
                          @PathVariable int itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("ItemController: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        return service.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable int itemId) {
        log.info("ItemController: обработка запроса на поиск вещи с id {}", itemId);
        return service.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findOwnersItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("ItemController: обработка запроса на поиск вещей пользователя с id {}", userId);
        return service.findOwnersItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("ItemController: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        return service.searchItems(text);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") int userId,
                       @PathVariable int itemId) {
        log.info("ItemController: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        service.delete(userId, itemId);
    }
}