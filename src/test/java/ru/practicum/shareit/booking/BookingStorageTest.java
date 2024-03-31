package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@DataJpaTest
public class BookingStorageTest {
    @Autowired
    BookingStorage bookingStorage;
    @Autowired
    ItemStorage itemStorage;
    @Autowired
    UserStorage userStorage;
    private final Sort sortStartDesc = Sort.by(DESC, "startDate");
    private final PageRequest pageRequest = PageRequest.of(0, 20, sortStartDesc);
    private User owner;
    private User booker;
    private Item item1;
    private Booking booking;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@user.com");
        owner = userStorage.save(owner);

        booker = new User();
        booker.setName("booker");
        booker.setEmail("booker@user.com");
        booker = userStorage.save(booker);

        item1 = new Item();
        item1.setName("item1");
        item1.setDescription("item1Desc");
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1 = itemStorage.save(item1);

        booking = new Booking();
        booking.setItem(item1);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setBooker(booker);
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));

        booking2 = new Booking();
        booking2.setItem(item1);
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setBooker(booker);
        booking2.setStartDate(LocalDateTime.now().plusDays(4));
        booking2.setEndDate(LocalDateTime.now().plusDays(5));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByItemOwnerId() {
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByItemOwnerId(owner.getId(),
                sortStartDesc);

        assertEquals(2, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
        assertEquals(booking.getId(), bookings.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter() {
        booking.setStartDate(LocalDateTime.now().minusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(1));
        booking = bookingStorage.save(booking);

        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
                owner.getId(), LocalDateTime.now(), LocalDateTime.now(), pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByItemOwnerIdAndEndDateBefore() {
        booking.setStartDate(LocalDateTime.now().minusDays(3));
        booking.setEndDate(LocalDateTime.now().minusDays(2));
        booking = bookingStorage.save(booking);
        booking2.setStartDate(LocalDateTime.now().minusDays(1));
        booking2.setEndDate(LocalDateTime.now().minusHours(4));
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndEndDateBefore(owner.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(2, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
        assertEquals(booking.getId(), bookings.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByItemOwnerIdAndStartDateAfter() {
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));
        booking2.setStartDate(LocalDateTime.now().plusDays(3));
        booking2.setEndDate(LocalDateTime.now().plusDays(4));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStartDateAfter(owner.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(2, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
        assertEquals(booking.getId(), bookings.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByItemOwnerIdAndStatus() {
        booking2.setStatus(BookingStatus.WAITING);
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStatus(owner.getId(), BookingStatus.APPROVED,
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());

        bookings = bookingStorage.findAllByItemOwnerIdAndStatus(owner.getId(), BookingStatus.WAITING, pageRequest)
                .getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());

        bookings = bookingStorage.findAllByItemOwnerIdAndStatus(owner.getId(), BookingStatus.REJECTED, pageRequest)
                .getContent();

        assertEquals(0, bookings.size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByBookerId() {
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByBookerId(booker.getId(), pageRequest).getContent();

        assertEquals(2, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
        assertEquals(booking.getId(), bookings.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByBookerIdAndStartDateBeforeAndEndDateAfter() {
        booking.setStartDate(LocalDateTime.now().minusDays(1));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStartDateBeforeAndEndDateAfter(booker.getId(),
                LocalDateTime.now(), LocalDateTime.now(), pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByBookerIdAndEndDateBefore() {
        booking.setStartDate(LocalDateTime.now().minusDays(4));
        booking.setEndDate(LocalDateTime.now().minusDays(2));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByBookerIdAndEndDateBefore(booker.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByBookerIdAndStartDateAfter() {
        booking.setStartDate(LocalDateTime.now().minusDays(1));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStartDateAfter(booker.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByBookerIdAndStatus() {
        booking2.setStatus(BookingStatus.WAITING);
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStatus(booker.getId(), BookingStatus.APPROVED,
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());

        bookings = bookingStorage.findAllByBookerIdAndStatus(booker.getId(), BookingStatus.WAITING, pageRequest)
                .getContent();

        assertEquals(1, bookings.size());
        assertEquals(booking2.getId(), bookings.get(0).getId());

        bookings = bookingStorage.findAllByBookerIdAndStatus(booker.getId(), BookingStatus.REJECTED, pageRequest)
                .getContent();

        assertEquals(0, bookings.size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindFirstByItemIdAndStatusNotAndStartDateAfter() {
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        Optional<Booking> nextBooking = bookingStorage.findFirstByItemIdAndStatusNotAndStartDateAfter(
                item1.getId(), BookingStatus.REJECTED, LocalDateTime.now(), Sort.by(ASC, "startDate")
        );

        assertEquals(booking.getId(), nextBooking.get().getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindFirstByItemIdAndStatusAndStartDateBefore() {
        booking.setStartDate(LocalDateTime.now().minusDays(5));
        booking.setEndDate(LocalDateTime.now().minusDays(4));
        booking2.setStartDate(LocalDateTime.now().minusDays(3));
        booking2.setEndDate(LocalDateTime.now().minusDays(2));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        Optional<Booking> lastBooking = bookingStorage.findFirstByItemIdAndStatusAndStartDateBefore(
                item1.getId(), BookingStatus.APPROVED, LocalDateTime.now(), Sort.by(DESC, "endDate"));

        assertEquals(booking2.getId(), lastBooking.get().getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindFirstByItemIdAndBookerIdAndStatusAndEndDateBefore() {
        booking.setStartDate(LocalDateTime.now().minusDays(5));
        booking.setEndDate(LocalDateTime.now().minusDays(4));
        booking = bookingStorage.save(booking);
        booking2 = bookingStorage.save(booking2);

        Optional<Booking> bookingOptional = bookingStorage.findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(
                item1.getId(), booker.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertEquals(booking.getId(), bookingOptional.get().getId());
    }
}
