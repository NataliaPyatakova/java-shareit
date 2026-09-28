package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingStateSearch;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto save(@RequestHeader("X-Sharer-User-Id") long userId,
                           @Validated @RequestBody NewBookingDto bookingDto) {
        return bookingService.save(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                             @PathVariable long bookingId,
                             @RequestParam boolean approved) {
        return bookingService.update(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable @NotNull long bookingId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findByBookerIdAndState(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam(value = "state", defaultValue = "ALL") BookingStateSearch state) {
        return bookingService.findByBookerIdAndState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(value = "state", defaultValue = "ALL")  BookingStateSearch state) {
        return bookingService.findByOwnerIdAndState(userId, state);
    }
}