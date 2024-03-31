package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    UserStorage userStorage;
    @Mock
    ItemRequestStorage itemRequestStorage;
    @Mock
    ItemStorage itemStorage;
    @InjectMocks
    ItemRequestServiceImpl requestService;
    User user;
    ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("user");
        user.setEmail("user@user.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("description");
        itemRequest.setCreated(LocalDateTime.now());
    }

    @Test
    void testAddItemRequest() {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("description");

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestStorage.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto responseDto = requestService.create(requestDto, user.getId());

        assertEquals(itemRequest.getId(), responseDto.getId());
        assertEquals(itemRequest.getDescription(), responseDto.getDescription());
        verify(itemRequestStorage, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void testAddItemRequestFailUserNotFound() {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("description");

        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> requestService.create(requestDto, user.getId()));

        assertEquals(String.format("Пользователь с id %d не найден", user.getId()), e.getMessage());
    }

    @Test
    void testFindAllByAuthorId() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemStorage.findByItemRequestIn(anyList(), any(Sort.class))).thenReturn(new ArrayList<>());
        when(itemRequestStorage.findAllByAuthorId(anyLong(), any(Sort.class))).thenReturn(List.of(itemRequest));

        List<ItemRequestResponseDto> responseDtoList = requestService.findAllByAuthorId(user.getId());

        assertEquals(1, responseDtoList.size());
        assertEquals(itemRequest.getId(), responseDtoList.get(0).getId());
        verify(itemRequestStorage, times(1)).findAllByAuthorId(eq(user.getId()), any(Sort.class));
    }

    @Test
    void testFindAllByAuthorIdFailUserNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> requestService.findAllByAuthorId(user.getId()));

        assertEquals(String.format("Пользователь с id %d не найден", user.getId()), e.getMessage());
    }

    @Test
    void testFindAllRequests() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestStorage.findAllByAuthorIdNot(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(itemRequest)));

        List<ItemRequestResponseDto> responseDtoList = requestService.findAllRequests(user.getId(), 0, 20);

        assertEquals(1, responseDtoList.size());
        assertEquals(itemRequest.getId(), responseDtoList.get(0).getId());
        verify(itemRequestStorage, times(1)).findAllByAuthorIdNot(eq(user.getId()), any(Pageable.class));
    }

    @Test
    void testFindById() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        ItemRequestResponseDto responseDto = requestService.findById(user.getId(), itemRequest.getId());

        assertEquals(itemRequest.getId(), responseDto.getId());
        verify(itemRequestStorage, times(1)).findById(itemRequest.getId());
    }

    @Test
    void testFindByIdItemRequestNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> requestService.findById(user.getId(), itemRequest.getId()));

        assertEquals(String.format("ItemRequest с id %d не найден", itemRequest.getId()), e.getMessage());
    }
}
