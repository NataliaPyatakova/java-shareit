package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingDto {

    long id;
    ItemDto item;
    UserDto booker;
    LocalDateTime start;
    LocalDateTime end;
    BookingProcessState status;
}
