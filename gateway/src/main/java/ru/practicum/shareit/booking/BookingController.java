package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingStateSearch;
import ru.practicum.shareit.booking.dto.NewBookingDto;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> save(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestBody @Valid NewBookingDto bookingDto) {
        log.info("Gateway booking save");
        return bookingClient.save(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable @NotNull long bookingId,
                                         @RequestParam boolean approved) {
        log.info("Gateway booking update");
        return bookingClient.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PathVariable @NotNull long bookingId) {
        log.info("Gateway booking findById");
        return bookingClient.findById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findByBookerIdAndState(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        log.info("Gateway booking findByBookerIdAndState");
        BookingStateSearch state = BookingStateSearch.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.findByBookerIdAndState(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        log.info("Gateway booking findByOwnerIdAndState");
        BookingStateSearch state = BookingStateSearch.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.findByOwnerIdAndState(userId, state);
    }
}