package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingStorage extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerId(Long ownerId, Sort sort);

    Page<Booking> findAllByItemOwnerId(Long ownerId, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfter(
            Long ownerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndDateBefore(Long ownerId, LocalDateTime endDate, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartDateAfter(Long ownerId, LocalDateTime startDate, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartDateBeforeAndEndDateAfter(
            Long bookerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndDateBefore(Long bookerId, LocalDateTime endDate, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartDateAfter(Long bookerId, LocalDateTime startDate, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    Optional<Booking> findFirstByItemIdAndStatusNotAndStartDateAfter(
            Long itemId, BookingStatus status, LocalDateTime startDate, Sort sort);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateBefore(
            Long itemId, BookingStatus status, LocalDateTime startDate, Sort sort);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndDateBefore(
            Long itemId, Long bookerId, BookingStatus bookingStatus, LocalDateTime endDate
    );
}