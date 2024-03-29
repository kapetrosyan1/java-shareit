package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotEnoughRightsException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.*;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage storage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;
    private final ItemRequestStorage itemRequestStorage;

    @Override
    @Transactional
    public ItemResponseDto create(Long userId, ItemRequestDto itemRequestDto) {
        log.info("ItemService: обработка запроса от пользователя {} на добавление вещи {}", userId,
                itemRequestDto.toString());
        Item item = ItemMapper.toItem(itemRequestDto);
        if (itemRequestDto.getRequestId() != null) {
            item.setItemRequest(itemRequestStorage.findById(itemRequestDto.getRequestId()).orElseThrow(() ->
                    new NotFoundException(String.format("ItemRequest с id %d не найден", itemRequestDto.getRequestId()))
            ));
        }
        item.setOwner(userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId))));
        return ItemMapper.toItemResponseDto(storage.save(item));
    }

    @Override
    @Transactional
    public ItemResponseDto update(Long userId, Long itemId, ItemRequestDto itemRequestDto) {
        log.info("ItemService: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException(
                String.format("Не удалось найти вещь с id %d", itemId)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotEnoughRightsException(String.format(
                    "Пользователь с id %d не является собственником вещи с id %d", userId, itemId));
        }
        Item updatedItem = ItemMapper.toItem(itemRequestDto);
        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        return ItemMapper.toItemResponseDto(storage.save(item));
    }

    @Override
    public ItemResponseDtoWithBookings findById(Long itemId, Long requesterId) {
        log.info("ItemService: обработка запроса на поиск вещи с id {}", itemId);
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException(
                String.format("Не удалось найти вещь с id %d", itemId)));
        if (item.getOwner().getId().equals(requesterId)) {
            ItemResponseDtoWithBookings itemDtoWithBookings = setLastAndNextBooking(item);
            itemDtoWithBookings.setComments(CommentMapper.toCommentResponseDtoList(commentStorage.findAllByItemId(itemId)));
            return itemDtoWithBookings;
        }
        ItemResponseDtoWithBookings itemDtoWithBookings = ItemMapper.toItemResponseDtoWithBookings(item);
        itemDtoWithBookings.setComments(CommentMapper.toCommentResponseDtoList(commentStorage.findAllByItemId(itemId)));
        return itemDtoWithBookings;
    }

    @Override
    public List<ItemResponseDtoWithBookings> findOwnersItems(Long userId, int from, int size) {
        log.info("ItemService: обработка запроса на поиск вещей пользователя с id {}", userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        PageRequest pageRequest = PageRequest.of((from / size), size, Sort.by(ASC, "id"));

        Map<Long, Item> items = storage.findAllByOwnerId(userId, pageRequest).getContent().stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        Map<Item, List<Comment>> comments = commentStorage.findByItemIn(new ArrayList<>(items.values()),
                        Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));

        Map<Item, List<Booking>> bookings = bookingStorage.findAllByItemOwnerId(userId,
                        Sort.by(DESC, "startDate"))
                .stream()
                .collect(groupingBy(Booking::getItem));

        return new ArrayList<>(items.values()).stream()
                .map(item -> setItemComments(item, comments))
                .peek(i -> i.setLastBooking(getLastBooking(bookings.get(items.get(i.getId())))))
                .peek(i -> i.setNextBooking(getNextBooking(bookings.get(items.get(i.getId())))))
                .collect(toList());
    }

    @Override
    public List<ItemResponseDto> searchItems(String text, int from, int size) {
        log.info("ItemService: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        PageRequest pageRequest = PageRequest.of((from / size), size);
        return storage.searchItems(text, pageRequest).getContent().stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long itemId) {
        log.info("ItemService: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException(
                String.format("Не удалось найти вещь с id %d", itemId)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotEnoughRightsException(
                    String.format("Пользователь с id %d не является собственником вещи с id %d", userId, itemId));
        }
        storage.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        log.info("ItemService: обработка запроса на добавление комментария к вещи с id {} от пользователя {}",
                itemId, userId);
        bookingStorage.findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(itemId, userId, BookingStatus.APPROVED,
                LocalDateTime.now()).orElseThrow(() -> new BadRequestException(
                String.format("Пользователь с id %d не арендовал вещь с id %d", userId, itemId)));
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException(
                String.format("Не удалось найти вещь с id %d", itemId)));
        User author = userStorage.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с id %d не найден", userId)));
        Comment comment = CommentMapper.toComment(commentRequestDto, author, item);
        return CommentMapper.toCommentResponseDto(commentStorage.save(comment));
    }

    private ItemResponseDtoWithBookings setItemComments(Item item, Map<Item, List<Comment>> comments) {
        if (comments.isEmpty() || comments.get(item) == null) {
            return ItemMapper.toItemResponseDtoWithBookings(item);
        }
        return ItemMapper.toItemResponseDtoWithBookings(item, CommentMapper.toCommentResponseDtoList(comments.get(item)));

    }

    private BookingResponseDto getLastBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        Optional<Booking> lastBooking = bookings.stream()
                .filter(booking -> booking.getStartDate().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getStartDate));
        return lastBooking.map(BookingMapper::toBookingResponseDto).orElse(null);
    }

    private BookingResponseDto getNextBooking(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        Optional<Booking> nextBooking = bookings.stream()
                .filter(booking -> booking.getStartDate().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStartDate));
        return nextBooking.map(BookingMapper::toBookingResponseDto).orElse(null);
    }

    private ItemResponseDtoWithBookings setLastAndNextBooking(Item item) {
        log.info("ItemService: добавление последнего и следующего бронирования к вещи {}", item.getId());
        ItemResponseDtoWithBookings itemDtoWithBookings = ItemMapper.toItemResponseDtoWithBookings(item);

        Optional<Booking> lastBooking = bookingStorage.findFirstByItemIdAndStatusAndStartDateBefore(
                item.getId(), BookingStatus.APPROVED, LocalDateTime.now(), Sort.by(DESC, "endDate")
        );

        lastBooking.ifPresent(booking -> itemDtoWithBookings.setLastBooking(BookingMapper.toBookingResponseDto(booking)));

        Optional<Booking> nextBooking = bookingStorage.findFirstByItemIdAndStatusNotAndStartDateAfter(
                item.getId(), BookingStatus.REJECTED, LocalDateTime.now(), Sort.by(ASC, "startDate")
        );

        nextBooking.ifPresent(booking -> itemDtoWithBookings.setNextBooking(BookingMapper.toBookingResponseDto(booking)));
        return itemDtoWithBookings;
    }
}