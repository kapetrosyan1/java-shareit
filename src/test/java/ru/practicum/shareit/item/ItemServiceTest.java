package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private CommentStorage commentStorage;

    private Item item;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("user");
        user.setEmail("user@user.com");

        item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(user);
    }

    @Test
    void testCreateItem() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("description");
        itemRequestDto.setAvailable(true);
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = itemService.create(1L, itemRequestDto);

        assertEquals(responseDto.getName(), itemRequestDto.getName());
        assertEquals(responseDto.getDescription(), itemRequestDto.getDescription());
        assertEquals(responseDto.getAvailable(), itemRequestDto.getAvailable());
        assertEquals(responseDto.getId(), 1L);
        verify(userStorage, times(1)).findById(1L);
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    void testCreateItemUserNotFound() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("description");
        itemRequestDto.setAvailable(true);
        when(userStorage.findById(anyLong())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> itemService.create(1L, itemRequestDto));
        verify(itemStorage, times(0)).save(any(Item.class));
    }

    @Test
    void testFindById() {
        when(bookingStorage.findFirstByItemIdAndStatusAndStartDateBefore(anyLong(), any(BookingStatus.class),
                any(LocalDateTime.class), any(Sort.class))).thenReturn(Optional.empty());
        when(bookingStorage.findFirstByItemIdAndStatusNotAndStartDateAfter(anyLong(), any(BookingStatus.class),
                any(LocalDateTime.class), any(Sort.class))).thenReturn(Optional.empty());
        when(commentStorage.findAllByItemId(anyLong())).thenReturn(new ArrayList<>());
        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));

        ItemResponseDtoWithBookings itemById = itemService.findById(1L, 1L);
        assertEquals(item.getId(), itemById.getId());
        verify(itemStorage, times(1)).findById(1L);
    }

    @Test
    void testFindByIdItemNotFound() {
        when(itemStorage.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(1L, 1L));
        verify(itemStorage, times(1)).findById(1L);
    }

    @Test
    void testUpdateItem() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("description");
        itemRequestDto.setAvailable(true);
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemStorage.save(any(Item.class))).thenReturn(item);

        ItemResponseDto itemResponseDto = itemService.update(1L, 1L, itemRequestDto);

        assertEquals(itemResponseDto.getName(), itemRequestDto.getName());
        assertEquals(itemResponseDto.getDescription(), itemRequestDto.getDescription());
        assertEquals(itemResponseDto.getAvailable(), itemRequestDto.getAvailable());
        assertEquals(itemResponseDto.getId(), 1L);
        verify(userStorage, times(1)).findById(1L);
        verify(itemStorage, times(1)).findById(1L);
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    void testFindAllOwners() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.findAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));

        List<ItemResponseDtoWithBookings> items = itemService.findOwnersItems(1L, 0, 20);

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
        verify(itemStorage, times(1)).findAllByOwnerId(eq(1L), any(Pageable.class));
    }

    @Test
    void testSearchItems() {
        when(itemStorage.searchItems(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));

        List<ItemResponseDto> items = itemService.searchItems("item", 0, 20);

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
        verify(itemStorage, times(1)).searchItems(eq("item"), any(Pageable.class));
    }

    @Test
    void testDeleteItem() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));

        assertDoesNotThrow(() -> itemService.delete(1L, 1L));
        verify(itemStorage, times(1)).deleteById(1L);
    }

    @Test
    void testAddComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("comment");
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentStorage.save(any(Comment.class))).thenReturn(comment);
        when(bookingStorage.findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(anyLong(), anyLong(),
                any(BookingStatus.class), any(LocalDateTime.class))).thenReturn(Optional.of(new Booking()));

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("comment");

        CommentResponseDto commentResponseDto = itemService.addComment(1L, 1L, commentRequestDto);
        assertEquals(commentResponseDto.getAuthorName(), user.getName());
        assertEquals(commentResponseDto.getText(), comment.getText());
        assertEquals(commentResponseDto.getId(), 1L);
        verify(commentStorage, times(1)).save(any(Comment.class));
    }
}