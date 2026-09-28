package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.enumeration.BookingStateSearch;

import java.util.List;

public interface BookingService {

    BookingDto save(NewBookingDto booking, long userId);

    BookingDto update(long bookingId, long userId, boolean approved);

    BookingDto findById(long bookingId, long userId);

    List<BookingDto> findByBookerIdAndState(long bookerId, BookingStateSearch state);

    List<BookingDto> findByOwnerIdAndState(long userId, BookingStateSearch state);

    void findPastBookingByBookerIdAndItemId(long bookerId, long itemId);

    List<BookingDto> findByItemIdAndState(long itemId, BookingProcessState state);

    List<BookingDto> findAllByItemIdInAndState(List<Long> itemIds, BookingProcessState state);
}
