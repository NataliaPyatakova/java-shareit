package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {

    public static BookingDto mapToBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setItem(ItemMapper.mapToItemDto(booking.getItem()));
        bookingDto.setBooker(UserMapper.mapToUserDto(booking.getBooker()));
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStart(booking.getStart());
        bookingDto.setStatus(booking.getState());
        return bookingDto;
    }

    public static Booking mapToBookingForCreate(NewBookingDto bookingDto, User user, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setState(BookingProcessState.WAITING);
        return booking;
    }
}