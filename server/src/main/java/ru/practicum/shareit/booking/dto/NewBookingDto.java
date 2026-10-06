package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class NewBookingDto {

    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}