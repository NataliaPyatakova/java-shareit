package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewBookingDto {

    @NotNull
    long itemId;
    @NotNull
    LocalDateTime start;
    @NotNull
    LocalDateTime end;
}
