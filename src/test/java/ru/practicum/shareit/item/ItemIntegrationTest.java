package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingMapper;
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
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        userStorage.save(newUser);

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

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindById() {
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

        assertThrows(NotFoundException.class, () -> itemService.findById(100L, 1L));

        ItemResponseDtoWithBookings itemResponseDtoWithBookings = itemService.findById(itemResponse.getId(),
                newUser.getId());

        assertEquals(itemResponseDtoWithBookings.getDescription(), requestDto.getDescription());
        assertNull(itemResponseDtoWithBookings.getLastBooking());
        assertNull(itemResponseDtoWithBookings.getNextBooking());

        Booking lastBooking = new Booking();
        lastBooking.setItem(itemStorage.findById(itemResponse.getId()).get());
        lastBooking.setBooker(userStorage.findById(newUser.getId()).get());
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setEndDate(LocalDateTime.now().minusDays(3));
        lastBooking.setStartDate(lastBooking.getEndDate().minusDays(1));
        lastBooking = bookingStorage.save(lastBooking);

        itemResponseDtoWithBookings = itemService.findById(itemResponse.getId(), newUser.getId());

        assertNull(itemResponseDtoWithBookings.getLastBooking());

        itemResponseDtoWithBookings = itemService.findById(itemResponse.getId(), user.getId());

        assertEquals(BookingMapper.toBookingResponseDto(lastBooking), itemResponseDtoWithBookings.getLastBooking());

        Booking nextBooking = new Booking();
        nextBooking.setItem(itemStorage.findById(itemResponse.getId()).get());
        nextBooking.setBooker(userStorage.findById(newUser.getId()).get());
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setEndDate(LocalDateTime.now().plusDays(5));
        nextBooking.setStartDate(nextBooking.getEndDate().minusDays(1));
        nextBooking = bookingStorage.save(nextBooking);

        itemResponseDtoWithBookings = itemService.findById(itemResponse.getId(), newUser.getId());

        assertNull(itemResponseDtoWithBookings.getLastBooking());
        assertNull(itemResponseDtoWithBookings.getNextBooking());

        itemResponseDtoWithBookings = itemService.findById(itemResponse.getId(), user.getId());

        assertEquals(BookingMapper.toBookingResponseDto(lastBooking), itemResponseDtoWithBookings.getLastBooking());
        assertEquals(BookingMapper.toBookingResponseDto(nextBooking), itemResponseDtoWithBookings.getNextBooking());
        assertNotNull(itemResponseDtoWithBookings.getComments());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindOwnersItems() {
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

        assertThrows(NotFoundException.class, () -> itemService.findOwnersItems(100L, 0, 20));

        List<ItemResponseDtoWithBookings> itemResponseDtoList = itemService.findOwnersItems(newUser.getId(), 0, 20);

        assertEquals(0, itemResponseDtoList.size());

        itemResponseDtoList = itemService.findOwnersItems(user.getId(), 0, 20);

        assertEquals(1, itemResponseDtoList.size());
        assertEquals(itemResponse.getDescription(), itemResponseDtoList.get(0).getDescription());
        assertNull(itemResponseDtoList.get(0).getLastBooking());
        assertNull(itemResponseDtoList.get(0).getNextBooking());
        assertEquals(0, itemResponseDtoList.get(0).getComments().size());

        Booking lastBooking = new Booking();
        lastBooking.setItem(itemStorage.findById(itemResponse.getId()).get());
        lastBooking.setBooker(userStorage.findById(newUser.getId()).get());
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setEndDate(LocalDateTime.now().minusDays(3));
        lastBooking.setStartDate(lastBooking.getEndDate().minusDays(1));
        lastBooking = bookingStorage.save(lastBooking);
        Booking nextBooking = new Booking();
        nextBooking.setItem(itemStorage.findById(itemResponse.getId()).get());
        nextBooking.setBooker(userStorage.findById(newUser.getId()).get());
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setEndDate(LocalDateTime.now().plusDays(5));
        nextBooking.setStartDate(nextBooking.getEndDate().minusDays(1));
        nextBooking = bookingStorage.save(nextBooking);

        itemResponseDtoList = itemService.findOwnersItems(user.getId(), 0, 20);

        assertEquals(1, itemResponseDtoList.size());
        assertEquals(BookingMapper.toBookingResponseDto(lastBooking), itemResponseDtoList.get(0).getLastBooking());
        assertEquals(BookingMapper.toBookingResponseDto(nextBooking), itemResponseDtoList.get(0).getNextBooking());
        assertEquals(0, itemResponseDtoList.get(0).getComments().size());

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("comment");
        CommentResponseDto commentResponseDto = itemService.addComment(newUser.getId(), itemResponse.getId(), commentRequestDto);

        itemResponseDtoList = itemService.findOwnersItems(user.getId(), 0, 20);

        assertEquals(1, itemResponseDtoList.size());
        assertEquals(BookingMapper.toBookingResponseDto(lastBooking), itemResponseDtoList.get(0).getLastBooking());
        assertEquals(BookingMapper.toBookingResponseDto(nextBooking), itemResponseDtoList.get(0).getNextBooking());
        assertEquals(List.of(commentResponseDto), itemResponseDtoList.get(0).getComments());

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setName("item2");
        requestDto2.setDescription("description");
        requestDto2.setAvailable(true);
        ItemResponseDto itemResponse2 = itemService.create(user.getId(), requestDto2);

        itemResponseDtoList = itemService.findOwnersItems(user.getId(), 0, 20);

        assertEquals(2, itemResponseDtoList.size());
        assertEquals(itemResponse.getId(), itemResponseDtoList.get(0).getId());
        assertEquals(itemResponse2.getId(), itemResponseDtoList.get(1).getId());
        assertEquals(BookingMapper.toBookingResponseDto(lastBooking), itemResponseDtoList.get(0).getLastBooking());
        assertEquals(BookingMapper.toBookingResponseDto(nextBooking), itemResponseDtoList.get(0).getNextBooking());
        assertEquals(List.of(commentResponseDto), itemResponseDtoList.get(0).getComments());
        assertEquals(0, itemResponseDtoList.get(1).getComments().size());

        ItemRequestDto requestDto3 = new ItemRequestDto();
        requestDto3.setName("item3");
        requestDto3.setDescription("description");
        requestDto3.setAvailable(true);
        ItemResponseDto itemResponse3 = itemService.create(newUser.getId(), requestDto3);

        itemResponseDtoList = itemService.findOwnersItems(newUser.getId(), 0, 20);

        assertEquals(1, itemResponseDtoList.size());
        assertEquals(itemResponse3.getId(), itemResponseDtoList.get(0).getId());
        assertNull(itemResponseDtoList.get(0).getLastBooking());
        assertNull(itemResponseDtoList.get(0).getNextBooking());
        assertEquals(0, itemResponseDtoList.get(0).getComments().size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testDelete() {
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
        userStorage.save(newUser);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.delete(100L, itemResponse.getId()));
        assertEquals("Пользователь с id 100 не найден", e.getMessage());

        e = assertThrows(NotFoundException.class, () -> itemService.delete(1L, 100L));
        assertEquals("Не удалось найти вещь с id 100", e.getMessage());

        NotEnoughRightsException exception = assertThrows(NotEnoughRightsException.class,
                () -> itemService.delete(2L, itemResponse.getId()));
        assertEquals("Пользователь с id 2 не является собственником вещи с id 1", exception.getMessage());
        assertEquals(1, itemService.findOwnersItems(user.getId(), 0, 20).size());

        itemService.delete(user.getId(), itemResponse.getId());

        assertEquals(0, itemService.findOwnersItems(user.getId(), 0, 20).size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testSearch() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("вещь");
        requestDto.setDescription("desc");
        requestDto.setAvailable(true);

        User user = new User();
        user.setName("user");
        user.setEmail("user@user.com");
        user = userStorage.save(user);

        ItemResponseDto itemResponse = itemService.create(user.getId(), requestDto);

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setName("iTem2");
        requestDto2.setDescription("description");
        requestDto2.setAvailable(true);
        ItemResponseDto itemResponse2 = itemService.create(user.getId(), requestDto2);

        ItemRequestDto requestDto3 = new ItemRequestDto();
        requestDto3.setName("item3");
        requestDto3.setDescription("описание");
        requestDto3.setAvailable(true);
        ItemResponseDto itemResponse3 = itemService.create(user.getId(), requestDto3);

        assertEquals(0, itemService.searchItems("", 0, 20).size());

        List<ItemResponseDto> items = itemService.searchItems("item", 0, 20);
        assertEquals(2, items.size());
        assertEquals(itemResponse2.getId(), items.get(0).getId());
        assertEquals(itemResponse3.getId(), items.get(1).getId());

        items = itemService.searchItems("вещь", 0, 20);
        assertEquals(1, items.size());
        assertEquals(itemResponse.getId(), items.get(0).getId());

        items = itemService.searchItems("desc", 0, 20);
        assertEquals(2, items.size());
        assertEquals(itemResponse.getId(), items.get(0).getId());
        assertEquals(itemResponse2.getId(), items.get(1).getId());
    }
}
