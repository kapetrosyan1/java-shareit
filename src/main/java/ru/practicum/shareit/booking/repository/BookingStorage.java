package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingStorage extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
            Long ownerId, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    List<Booking> findAllByItemOwnerIdAndEndDateBefore(Long ownerId, LocalDateTime endDate, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartDateAfter(Long ownerId, LocalDateTime startDate, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndStartDateBeforeAndEndDateAfter(
            Long bookerId, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    List<Booking> findAllByBookerIdAndEndDateBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    List<Booking> findAllByBookerIdAndStartDateAfter(Long bookerId, LocalDateTime startDate, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusNotAndStartDateAfter(
            Long itemId, BookingStatus status, LocalDateTime startDate, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateBefore(
            Long itemId, BookingStatus status, LocalDateTime startDate, Sort sort);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(
            Long itemId, Long bookerId, BookingStatus bookingStatus, LocalDateTime endDate
    );
}