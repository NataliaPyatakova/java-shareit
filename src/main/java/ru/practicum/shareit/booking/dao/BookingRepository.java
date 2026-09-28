package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemUserIdOrderByStartDesc(long userId);

    List<Booking> findAllByItemUserIdAndStateOrderByStartDesc(long userId, BookingProcessState state);

    List<Booking> findAllByItemUserIdAndStartBeforeAndEndAfterOrderByStartDesc(Long itemUserId, LocalDateTime startBefore, LocalDateTime endAfter);

    List<Booking> findAllByItemUserIdAndEndBeforeOrderByStartDesc(Long itemUserId, LocalDateTime endBefore);

    List<Booking> findAllByItemUserIdAndStartAfterOrderByStartDesc(Long itemUserId, LocalDateTime startAfter);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByBookerIdAndStateOrderByStartDesc(long bookerId, BookingProcessState state);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime startBefore, LocalDateTime endAfter);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime endBefore);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime startAfter);

    Optional<Booking> findBookingByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime endBefore);

    List<Booking> findAllByItemIdAndState(Long itemId, BookingProcessState state);

    List<Booking> findAllByItemIdInAndState(List<Long> itemIds, BookingProcessState state);
}
