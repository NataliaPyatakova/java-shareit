package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingStateSearch {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingStateSearch> from(String stringState) {
        for (BookingStateSearch state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
