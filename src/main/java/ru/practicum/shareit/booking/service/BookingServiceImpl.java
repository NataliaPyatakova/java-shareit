package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.enumeration.BookingStateSearch;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.WrongUserException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto save(NewBookingDto newBookingDto, long userId) {
        log.info("save booking {} for userId {}", newBookingDto, userId);
        validateBooking(newBookingDto);
        User user = UserMapper.mapToUser(userService.findById(userId));
        Item item = itemRepository.findById(newBookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + newBookingDto.getItemId() + " не найдена"));
        if (!item.getAvailable()) {
            throw new BadRequestException("Бронируемая вещь занята!");
        }
        Booking booking = BookingMapper.mapToBookingForCreate(newBookingDto, user, item);
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto update(long bookingId, long userId, boolean approved) {
        log.info("update booking {} from userId {} status {}", bookingId, userId, approved);
        Booking booking = findById(bookingId);
        if (!Objects.equals(userId, booking.getItem().getUser().getId())) {
            throw new WrongUserException("Изменять статус бронирования может только владелец вещи");
        }
        booking.setState(approved ? BookingProcessState.APPROVED : BookingProcessState.REJECTED);
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        log.info("find booking {} to userId {}", bookingId, userId);
        Booking booking = findById(bookingId);
        if (booking.getItem().getUser().getId() != userId && booking.getBooker().getId() != userId) {
            throw new WrongUserException("Просматривать бронирование может только владелец вещи или автор бронирования");
        }
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> findByBookerIdAndState(long bookerId, BookingStateSearch state) {
        log.info("find bookings for booker {} with status {}", bookerId, state);
        userService.findById(bookerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookingList = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
            case CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, now);
            case WAITING ->
                    bookingRepository.findAllByBookerIdAndStateOrderByStartDesc(bookerId, BookingProcessState.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStateOrderByStartDesc(bookerId, BookingProcessState.REJECTED);
        };
        return bookingList.stream().map(BookingMapper::mapToBookingDto).toList();
    }

    @Override
    public List<BookingDto> findByOwnerIdAndState(long userId, BookingStateSearch state) {
        log.info("find bookings for ownerId {} with status {}", userId, state);
        userService.findById(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookingList = switch (state) {
            case ALL -> bookingRepository.findAllByItemUserIdOrderByStartDesc(userId);
            case CURRENT ->
                    bookingRepository.findAllByItemUserIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findAllByItemUserIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findAllByItemUserIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING ->
                    bookingRepository.findAllByItemUserIdAndStateOrderByStartDesc(userId, BookingProcessState.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByItemUserIdAndStateOrderByStartDesc(userId, BookingProcessState.REJECTED);
        };
        return bookingList.stream().map(BookingMapper::mapToBookingDto).toList();
    }

    @Override
    public void findPastBookingByBookerIdAndItemId(long bookerId, long itemId) {
        log.info("findPastBookingByBookerIdAndItemId bookerId {} itemId {}", bookerId, itemId);
        bookingRepository.findBookingByBookerIdAndItemIdAndEndBefore(bookerId, itemId, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Бронирование этой вещи данным пользователем не найдено"));
    }

    @Override
    public List<BookingDto> findByItemIdAndState(long itemId, BookingProcessState state) {
        log.info("findByItemIdAndState itemId {} state {}", itemId, state);
        return bookingRepository.findAllByItemIdAndState(itemId,state).stream().map(BookingMapper::mapToBookingDto).toList();
    }

    @Override
    public List<BookingDto> findAllByItemIdInAndState(List<Long> itemIds, BookingProcessState state) {
        log.info("findAllByItemIdInAndState state {}", state);
        return bookingRepository.findAllByItemIdInAndState(itemIds, state).stream().map(BookingMapper::mapToBookingDto).toList();
    }

    private Booking findById(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
    }

    private static void validateBooking(NewBookingDto newBookingDto) {
        log.info("validateBooking newBookingDto {}", newBookingDto);
        if (newBookingDto.getEnd().equals(newBookingDto.getStart())) {
            throw new BadRequestException("Дата окончания бронирования не может быть равна дате начала");
        }
        if (newBookingDto.getEnd().isBefore(newBookingDto.getStart())) {
            throw new BadRequestException("Дата окончания бронирования не может быть раньше даты начала");
        }
        if (newBookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата окончания бронирования не может быть раньше текущей даты");
        }
        if (newBookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата начала бронирования не может быть раньше текущей даты");
        }
    }
}