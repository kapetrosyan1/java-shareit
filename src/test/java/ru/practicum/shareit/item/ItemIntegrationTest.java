package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotEnoughRightsException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.comment.CommentStorage;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ItemIntegrationTest {
    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private ItemRequestStorage itemRequestStorage;
    @Autowired
    private CommentStorage commentStorage;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private BookingStorage bookingStorage;
    @Autowired
    private ItemStorage itemStorage;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCreateItem() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("item");
        requestDto.setDescription("desc");
        requestDto.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.create(1L, requestDto));

        User user = new User();
        user.setName("user");
        user.setEmail("user@user.com");
        user = userStorage.save(user);

        ItemResponseDto itemResponse = itemService.create(user.getId(), requestDto);

        assertEquals(itemResponse.getDescription(), itemService.findById(itemResponse.getId(), user.getId()).getDescription());

        User newUser = new User();
        newUser.setName("newUser");
        newUser.setEmail("newUser@user.com");
        newUser = userStorage.save(newUser);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setAuthor(newUser);
        itemRequest.setDescription("request");
        itemRequest = itemRequestStorage.save(itemRequest);

        ItemRequestDto answerDto = new ItemRequestDto();
        answerDto.setName("answer");
        answerDto.setDescription("answerToReq");
        answerDto.setAvailable(true);
        answerDto.setRequestId(itemRequest.getId());

        ItemResponseDto newItem = itemService.create(user.getId(), answerDto);

        assertEquals(itemRequest.getId(), itemService.findById(newItem.getId(), user.getId()).getRequestId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testUpdate() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("item");
        requestDto.setDescription("desc");
        requestDto.setAvailable(true);

        User user = new User();
        user.setName("user");
        user.setEmail("user@user.com");
        user = userStorage.save(user);

        ItemResponseDto itemResponse = itemService.create(user.getId(), requestDto);

        User newUser = new User();
        newUser.setName("newUser");
        newUser.setEmail("newUser@user.com");
        newUser = userStorage.save(newUser);

        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setDescription("update Desc");

        NotFoundException e = assertThrows(NotFoundException.class, () -> itemService.update(100L,
                1L, updateDto));

        assertEquals("Пользователь с id 100 не найден", e.getMessage());

        e = assertThrows(NotFoundException.class, () -> itemService.update(1L,
                100L, updateDto));

        assertEquals("Не удалось найти вещь с id 100", e.getMessage());

        NotEnoughRightsException exception = assertThrows(NotEnoughRightsException.class,
                () -> itemService.update(2L, 1L, updateDto));

        assertEquals("Пользователь с id 2 не является собственником вещи с id 1", exception.getMessage());

        itemResponse = itemService.update(user.getId(), itemResponse.getId(), updateDto);

        assertEquals(updateDto.getDescription(), itemService.findById(itemResponse.getId(), user.getId()).getDescription());

        ItemRequestDto updateDto2 = new ItemRequestDto();
        updateDto2.setName("New Name");

        itemResponse = itemService.update(user.getId(), itemResponse.getId(), updateDto2);

        assertEquals(updateDto.getDescription(), itemService.findById(itemResponse.getId(), user.getId()).getDescription());
        assertEquals(updateDto2.getName(), itemService.findById(itemResponse.getId(), user.getId()).getName());

        ItemRequestDto updateDto3 = new ItemRequestDto();
        updateDto3.setAvailable(false);

        itemResponse = itemService.update(user.getId(), itemResponse.getId(), updateDto3);

        assertEquals(updateDto.getDescription(), itemService.findById(itemResponse.getId(), user.getId()).getDescription());
        assertEquals(updateDto2.getName(), itemService.findById(itemResponse.getId(), user.getId()).getName());
        assertEquals(updateDto3.getAvailable(), itemService.findById(itemResponse.getId(), user.getId()).getAvailable());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAddComment() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("item");
        requestDto.setDescription("desc");
        requestDto.setAvailable(true);

        User user = new User();
        user.setName("user");
        user.setEmail("user@user.com");
        user = userStorage.save(user);

        ItemResponseDto itemResponse = itemService.create(user.getId(), requestDto);

        User newUser = new User();
        newUser.setName("newUser");
        newUser.setEmail("newUser@user.com");
        newUser = userStorage.save(newUser);

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("comment");

        BadRequestException e = assertThrows(BadRequestException.class, () -> itemService.addComment(2L,
                1L, commentRequestDto));

        assertEquals("Пользователь с id 2 не арендовал вещь с id 1", e.getMessage());

        Booking booking = new Booking();
        booking.setItem(itemStorage.findById(itemResponse.getId()).get());
        booking.setBooker(userStorage.findById(2L).get());
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusDays(5));
        booking.setEndDate(LocalDateTime.now().minusDays(3));
        bookingStorage.save(booking);

        CommentResponseDto commentResponseDto = itemService.addComment(newUser.getId(), itemResponse.getId(),
                commentRequestDto);

        assertEquals(commentRequestDto.getText(), commentStorage.findById(commentResponseDto.getId()).get().getText());
    }
}
