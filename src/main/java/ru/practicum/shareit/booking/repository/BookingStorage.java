package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingStorage extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerIdOrderByStartDateDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long ownerId, LocalDateTime now,
                                                                                            LocalDateTime now2);

    List<Booking> findAllByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDateDesc(Long ownerId, BookingStatus status);

    List<Booking> findAllByBookerIdOrderByStartDateDesc(Long bookerId); // All

    List<Booking> findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long bookerId,
                                                                                         LocalDateTime now,
                                                                                         LocalDateTime now2); // CURRENT

    List<Booking> findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(Long bookerId, LocalDateTime now); // PAST

    List<Booking> findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(Long bookerId, LocalDateTime now); //FUTURE

    List<Booking> findAllByBookerIdAndStatusOrderByStartDateDesc(Long bookerId, BookingStatus status); // WAITING, REJECTED

    Optional<Booking> findFirstByItemIdAndStatusNotAndStartDateAfterOrderByStartDateAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateBeforeOrderByEndDateDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(
            Long itemId, Long bookerId, BookingStatus bookingStatus, LocalDateTime now
    );
}