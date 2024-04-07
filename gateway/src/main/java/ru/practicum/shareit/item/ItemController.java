package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.Constant.USER_HEADER;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_HEADER) @Positive Long userId,
                                          @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("ITEM_GATEWAY: add item {} from user userId={}", itemRequestDto, userId);
        return itemClient.addItem(userId, itemRequestDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> commentItem(@RequestHeader(USER_HEADER) @Positive Long userId,
                                              @PathVariable @Positive Long itemId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        log.info("ITEM_GATEWAY: add comment {} to item itemId={} from user userId={}", commentRequestDto, itemId, userId);
        return itemClient.commentItem(userId, itemId, commentRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_HEADER) @Positive Long userId,
                                             @PathVariable @Positive Long itemId,
                                             @RequestBody ItemRequestDto itemRequestDto) {
        log.info("ITEM_GATEWAY: update item itemId={} from user userId={} by dto {}", itemId, userId, itemRequestDto);
        return itemClient.updateItem(userId, itemId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_HEADER) @Positive Long userId,
                                          @PathVariable @Positive Long itemId) {
        log.info("ITEM_GATEWAY: get item itemId={} request from user userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(USER_HEADER) @Positive Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @PositiveOrZero int size) {
        log.info("ITEM_GATEWAY: find owner ownerId={} items, page from={} size={}", userId, from, size);
        return itemClient.getOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_HEADER) @Positive Long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @PositiveOrZero int size) {
        log.info("ITEM_GATEWAY: search items by string {}, page from={} size={}", text, from, size);
        return itemClient.search(userId, text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader(USER_HEADER) @Positive Long userId,
                                             @PathVariable @Positive Long itemId) {
        log.info("ITEM_GATEWAY: delete item itemId={} request from user userId={}", itemId, userId);
        return itemClient.deleteItem(userId, itemId);
    }
}