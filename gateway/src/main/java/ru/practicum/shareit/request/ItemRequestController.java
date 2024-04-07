package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.Constant.USER_HEADER;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(USER_HEADER) @Positive Long userId,
                                                 @RequestBody @Valid ItemRequestRequestDto itemRequestRequestDto) {
        log.info("ITEM_REQUEST_GATEWAY: add itemRequest {} from user userId={}", itemRequestRequestDto, userId);
        return itemRequestClient.addItemRequest(userId, itemRequestRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByAuthor(@RequestHeader(USER_HEADER) @Positive Long userId) {
        log.info("ITEM_REQUEST_GATEWAY: get requests by authorId={}", userId);
        return itemRequestClient.getRequestsByAuthor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getItemRequests(@RequestHeader(USER_HEADER) @Positive Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("ITEM_REQUEST_GATEWAY: get all requests from user userId={}, page from={}, size={}", userId, from, size);
        return itemRequestClient.getItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(USER_HEADER) @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("ITEM_REQUEST_GATEWAY: get itemRequest by id={} from user userId={}", requestId, userId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}