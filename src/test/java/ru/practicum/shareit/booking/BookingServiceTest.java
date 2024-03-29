package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.exception.exceptions.BadRequestException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
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
public class BookingServiceTest {
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private ItemStorage itemStorage;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private User owner;
    private User booker;
    private Item item1;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("owner");
        owner.setEmail("owner@user.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("booker");
        booker.setEmail("booker@user.com");

        item1 = new Item();
        item1.setId(1L);
        item1.setOwner(owner);
        item1.setName("item1");
        item1.setDescription("item1");
        item1.setAvailable(true);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item1);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStartDate(LocalDateTime.now().plusMonths(1));
        booking.setEndDate(LocalDateTime.now().plusMonths(1).plusDays(1));
    }


    @Test
    void testAddRequest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingStorage.save(any(Booking.class))).thenReturn(booking);

        BookingDtoWithItemAndUser responseDto = bookingService.addRequest(bookingRequestDto, 2L);
        assertEquals(booking.getId(), responseDto.getId());
        assertEquals(BookingStatus.WAITING, responseDto.getStatus());
        verify(bookingStorage, times(1)).save(any(Booking.class));
    }

    @Test
    void testAddRequestFailEndBeforeStart() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now());
        bookingRequestDto.setEnd(LocalDateTime.now().minusHours(5));

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> bookingService.addRequest(bookingRequestDto, booker.getId()));
        assertEquals("Дата завершения бронирования должна быть позже даты начала", e.getMessage());
    }

    @Test
    void testAddRequestFailItemNotFound() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        when(itemStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addRequest(bookingRequestDto, booker.getId()));
        assertEquals(String.format("Не удалось найти вещь с id %d", bookingRequestDto.getItemId()), e.getMessage());
    }

    @Test
    void testAddRequestFailBookerNotFound() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item1));
        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addRequest(bookingRequestDto, 2L));
        assertEquals(String.format("Пользователь с id %d не найден", booker.getId()), e.getMessage());
    }

    @Test
    void testAddRequestFailItemUnavailable() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(2L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(owner);
        item2.setName("item2");
        item2.setDescription("anotherItem");
        item2.setAvailable(false);

        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item2));
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> bookingService.addRequest(bookingRequestDto, 2L));
        assertEquals(String.format("Вещь c id %d недоступна для аренды", item2.getId()), e.getMessage());
    }

    @Test
    void testAddRequestFailBookerIsOwner() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item1));
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(owner));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addRequest(bookingRequestDto, 1L));
        assertEquals("Собственник не может арендовать свою вещь", e.getMessage());
    }

    @Test
    void testUpdateBooking() {
        when(bookingStorage.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingStorage.save(any(Booking.class))).thenReturn(booking);

        BookingDtoWithItemAndUser responseDto = bookingService.update(1L, 1L, true);
        assertEquals(BookingStatus.APPROVED, responseDto.getStatus());
        verify(bookingStorage, times(1)).save(any(Booking.class));
    }

    @Test
    void testUpdateBookingFailUserNotOwner() {
        when(bookingStorage.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.update(2L, 1L, true));
        assertEquals(String.format("Пользователь %d не является собственником бронируемой вещи", booker.getId()),
                e.getMessage());
    }

    @Test
    void testUpdateBookingFailNotWaiting() {
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingStorage.findById(anyLong())).thenReturn(Optional.of(booking));

        BadRequestException e = assertThrows(BadRequestException.class, () -> bookingService.update(1L,
                1L, true));
        assertEquals(String.format("На бронирование %d уже был дан ответ", booking.getId()), e.getMessage());
    }

    @Test
    void testFindById() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingStorage.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDtoWithItemAndUser response = bookingService.findById(owner.getId(), booking.getId());

        assertEquals(response.getId(), booking.getId());
        assertEquals(response.getStart(), booking.getStartDate());
        assertEquals(response.getEnd(), booking.getEndDate());
        verify(bookingStorage, times(1)).findById(booking.getId());
    }

    @Test
    void testFindByIdFailRequesterNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.findById(3L, 1L));
        assertEquals(String.format("Пользователь с id %d не найден", 3L), e.getMessage());
    }

    @Test
    void testFindByIdFailBookingNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.findById(booker.getId(), booking.getId()));
        assertEquals(String.format("Бронирование с id %d не найдено", booking.getId()), e.getMessage());
    }

    @Test
    void testFindByIdFailRequesterNotOwnerOrBooker() {
        User user3 = new User();
        user3.setId(3L);
        user3.setName("3");
        user3.setEmail("3@mail.com");

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user3));
        when(bookingStorage.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.findById(user3.getId(), booking.getId()));
        assertEquals(String.format("Пользователь с id %d не является арендатором или собственником вещи", user3.getId()),
                e.getMessage());
    }

    @Test
    void testFindAllByUserAndStateAll() {
        Booking booking1 = new Booking();
        booking1.setId(2L);
        booking1.setItem(item1);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.REJECTED);
        booking1.setStartDate(LocalDateTime.now().minusMonths(1));
        booking1.setEndDate(LocalDateTime.now().minusMonths(1).plusDays(1));

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerId(anyLong(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(booking, booking1)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(booker.getId(), "ALL",
                0, 20);

        assertEquals(2, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        assertEquals(booking1.getId(), bookings.get(1).getId());
        assertEquals(booking1.getStartDate(), bookings.get(1).getStart());
        assertEquals(booking1.getEndDate(), bookings.get(1).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerId(eq(booker.getId()),
                any(Pageable.class));
    }

    @Test
    void testFindAllByUserAndStateCurrent() {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(4));

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerIdAndStartDateBeforeAndEndDateAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(
                booker.getId(), "CURRENT", 0, 20
        );

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerIdAndStartDateBeforeAndEndDateAfter(
                eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByUserAndStatePast() {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusHours(5));
        booking.setEndDate(LocalDateTime.now().plusHours(2));

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerIdAndEndDateBefore(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(booker.getId(), "PAST",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerIdAndEndDateBefore(
                eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByUserAndStateFuture() {
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerIdAndStartDateAfter(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(booker.getId(), "FUTURE",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerIdAndStartDateAfter(
                eq(booker.getId()), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByUserAndStateWaiting() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerIdAndStatus(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(booker.getId(), "WAITING",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerIdAndStatus(
                eq(booker.getId()), eq(BookingStatus.WAITING), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByUserAndStateRejected() {
        booking.setStatus(BookingStatus.REJECTED);

        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingStorage.findAllByBookerIdAndStatus(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByUserAndState(booker.getId(), "REJECTED",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByBookerIdAndStatus(
                eq(booker.getId()), eq(BookingStatus.REJECTED), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByUserAndStateFailedUserNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> bookingService.findAllByUserAndState(99L,
                "WAITING", 0, 20));
        assertEquals("Пользователь с id 99 не найден", e.getMessage());
    }

    @Test
    void testFindAllByUserAndStateFailedUnsupportedStatus() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(booker));
        BadRequestException e = assertThrows(BadRequestException.class, () -> bookingService.findAllByUserAndState(
                booker.getId(), "UNSUPPORTED_STATUS", 0, 20
        ));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
    }

    @Test
    void testFindAllByOwnerAndStateAll() {
        Booking booking1 = new Booking();
        booking1.setId(2L);
        booking1.setItem(item1);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.REJECTED);
        booking1.setStartDate(LocalDateTime.now().minusMonths(1));
        booking1.setEndDate(LocalDateTime.now().minusMonths(1).plusDays(1));

        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId(), booking1.getId()));
        when(bookingStorage.findAllByItemOwnerId(anyLong(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(booking, booking1)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "ALL",
                0, 20);

        assertEquals(2, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        assertEquals(booking1.getId(), bookings.get(1).getId());
        assertEquals(booking1.getStartDate(), bookings.get(1).getStart());
        assertEquals(booking1.getEndDate(), bookings.get(1).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerId(eq(owner.getId()),
                any(Pageable.class));
    }

    @Test
    void testFindAllByOwnerAndStateCurrent() {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusHours(1));
        booking.setEndDate(LocalDateTime.now().plusHours(4));

        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        when(bookingStorage.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "CURRENT",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
                eq(owner.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByOwnerAndStatePast() {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusHours(5));
        booking.setEndDate(LocalDateTime.now().plusHours(2));

        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        when(bookingStorage.findAllByItemOwnerIdAndEndDateBefore(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "PAST",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerIdAndEndDateBefore(
                eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByOwnerAndStateFuture() {
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));

        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        when(bookingStorage.findAllByItemOwnerIdAndStartDateAfter(anyLong(), any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "FUTURE",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerIdAndStartDateAfter(
                eq(owner.getId()), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByOwnerAndStateWaiting() {
        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        when(bookingStorage.findAllByItemOwnerIdAndStatus(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "WAITING",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerIdAndStatus(
                eq(owner.getId()), eq(BookingStatus.WAITING), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByOwnerAndStateRejected() {
        booking.setStatus(BookingStatus.REJECTED);

        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        when(bookingStorage.findAllByItemOwnerIdAndStatus(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDtoWithItemAndUser> bookings = bookingService.findAllByOwnerAndState(owner.getId(), "REJECTED",
                0, 20);

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
        assertEquals(booking.getStartDate(), bookings.get(0).getStart());
        assertEquals(booking.getEndDate(), bookings.get(0).getEnd());
        verify(bookingStorage, times(1)).findAllByItemOwnerIdAndStatus(
                eq(owner.getId()), eq(BookingStatus.REJECTED), any(Pageable.class)
        );
    }

    @Test
    void testFindAllByOwnerAndStateFailNoItems() {
        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(new ArrayList<>());

        NotFoundException e = assertThrows(NotFoundException.class, () -> bookingService.findAllByOwnerAndState(
                99L, "ALL", 0, 20
        ));
        assertEquals("У пользователя с id 99 нет вещей в собственности", e.getMessage());
    }

    @Test
    void testFindAllByOwnerAndStateFainNUnsupportedStatus() {
        when(itemStorage.findAllOwnersItemsIds(anyLong())).thenReturn(List.of(booking.getId()));
        BadRequestException e = assertThrows(BadRequestException.class, () -> bookingService.findAllByOwnerAndState(
                owner.getId(), "UNSUPPORTED_STATUS", 0, 20
        ));
        assertEquals("Unknown state: UNSUPPORTED_STATUS", e.getMessage());
    }
}