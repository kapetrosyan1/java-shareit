package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotEnoughRightsException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage storage;
    private final UserService userService;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("ItemService: обработка запроса от пользователя " + userId + " на добавление вещи " + itemDto.toString());
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(UserMapper.toUser(userService.findById(userId)));
        return ItemMapper.toItemDto(storage.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("ItemService: обработка запроса на обновление вещи с id {} от пользователя {}", itemId, userId);
        userService.findById(userId);
        Item item = storage.findByIdWithOwner(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId +
                " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotEnoughRightsException("Пользователь с id " + userId + " не является собственником вещи с id " +
                    itemId);
        }
        Item updatedItem = ItemMapper.toItem(itemDto);
        if (updatedItem.getName() != null) {
            item.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            item.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            item.setAvailable(updatedItem.getAvailable());
        }
        return ItemMapper.toItemDto(storage.save(item));
    }

    @Override
    public ItemDtoWithBookings findById(Long itemId, Long requesterId) {
        log.info("ItemService: обработка запроса на поиск вещи с id {}", itemId);
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException(
                "Не удалось найти вещь с id " + itemId));
        if (item.getOwner().getId().equals(requesterId)) {
            ItemDtoWithBookings itemDtoWithBookings = setLastAndNextBooking(item);
            itemDtoWithBookings.setComments(CommentMapper.toCommentDtoList(commentStorage.findAllByItemId(itemId)));
            return itemDtoWithBookings;
        }
        ItemDtoWithBookings itemDtoWithBookings = ItemMapper.toItemDtoWithBookings(item);
        itemDtoWithBookings.setComments(CommentMapper.toCommentDtoList(commentStorage.findAllByItemId(itemId)));
        return itemDtoWithBookings;
    }

    @Override
    public List<ItemDtoWithBookings> findOwnersItems(Long userId) {
        log.info("ItemService: обработка запроса на поиск вещей пользователя с id {}", userId);
        userService.findById(userId);
        List<Item> ownerItems = storage.findAllByOwnerIdOrderByIdAsc(userId);
        return ownerItems.stream()
                .map(this::setLastAndNextBooking)
                .peek(itemDto -> itemDto.setComments(CommentMapper.toCommentDtoList(
                        commentStorage.findAllByItemId(itemDto.getId()))))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("ItemService: обработка запроса на поиск вещей, содержащих в названии или описании фрагмент {}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return storage.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId, Long itemId) {
        log.info("ItemService: обработка запроса на удаление вещи с id {} от пользователя {}", itemId, userId);
        userService.findById(userId);
        Item item = storage.findByIdWithOwner(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId +
                " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotEnoughRightsException("Пользователь с id " + userId + " не является собственником вещи с id " +
                    itemId);
        }
        storage.deleteById(itemId);
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("ItemService: обработка запроса на добавление комментария к вещи с id {} от пользователя {}",
                itemId, userId);
        bookingStorage.findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(itemId, userId, BookingStatus.APPROVED,
                LocalDateTime.now()).orElseThrow(() -> new BadRequestException("Пользователь " + userId +
                " не брал в аренду вещь " + itemId));
        Item item = storage.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId +
                " не существует"));
        User author = UserMapper.toUser(userService.findById(userId));
        Comment comment = CommentMapper.toComment(commentDto, author, item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentStorage.save(comment));
    }

    private ItemDtoWithBookings setLastAndNextBooking(Item item) {
        log.info("ItemService: добавление последнего и следующего бронирования к вещи {}", item.getId());
        ItemDtoWithBookings itemDtoWithBookings = ItemMapper.toItemDtoWithBookings(item);

        Optional<Booking> lastBooking = bookingStorage.findFirstByItemIdAndStatusAndStartDateBeforeOrderByEndDateDesc(
                item.getId(), BookingStatus.APPROVED, LocalDateTime.now()
        );

        lastBooking.ifPresent(booking -> itemDtoWithBookings.setLastBooking(BookingMapper.toBookingDto(booking)));

        Optional<Booking> nextBooking = bookingStorage.findFirstByItemIdAndStatusNotAndStartDateAfterOrderByStartDateAsc(
                item.getId(), BookingStatus.REJECTED, LocalDateTime.now()
        );

        nextBooking.ifPresent(booking -> itemDtoWithBookings.setNextBooking(BookingMapper.toBookingDto(booking)));
        return itemDtoWithBookings;
    }
}