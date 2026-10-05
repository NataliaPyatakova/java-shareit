package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.enumeration.BookingStateSearch;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.WrongUserException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
@SpringBootTest
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemRepository itemRepository;

    private final long TIMEOUT = 2;
    private final LocalDateTime date = LocalDateTime.now();
    private final NewUserDto owner = new NewUserDto()
            .setName("test_user")
            .setEmail("test_user@test.ru");
    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setStart(date)
            .setEnd(date.plusSeconds(2));
    private final NewBookingDto newBookingDtoFuture = new NewBookingDto()
            .setStart(date.plusDays(1))
            .setEnd(date.plusDays(1).plusSeconds(2));
    private final Item item = new Item()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);
    private final NewUserDto booker = new NewUserDto()
            .setName("test_booker")
            .setEmail("test_booker@test.ru");

    @Test
    @DisplayName("Сохранение бронирования - все хорошо")
    public void testSaveAllOk() {
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto result = bookingService.save(newBookingDto, savedBooker.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newBookingDto.getItemId(), result.getItem().getId());
        Assertions.assertEquals(savedBooker.getId(), result.getBooker().getId());
        Assertions.assertEquals(newBookingDto.getStart(), result.getStart());
        Assertions.assertEquals(newBookingDto.getEnd(), result.getEnd());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getStatus());
    }

    @Test
    @DisplayName("Сохранение бронирования - несуществующий пользователь")
    public void testSaveWrongUser() {
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());

        try {
            bookingService.save(newBookingDto, 999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение бронирования - несуществующая вещь")
    public void testSaveWrongItem() {
        newBookingDto.setItemId(999L);
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение бронирования - вещь занята")
    public void testSaveNotAvailable() {
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        item.setAvailable(false);
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Бронируемая вещь занята!", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Валидация дат - даты равны")
    public void testValidateEqualDates() {
        //уравниваем даты, чтобы попасть в ошибку
        newBookingDto.setStart(date);
        newBookingDto.setEnd(date);
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Дата окончания бронирования не может быть равна дате начала",
                    ex.getMessage());
        }
    }

    @Test
    @DisplayName("Валидация дат - дата окончания раньше даты начала")
    public void testValidateEndBeforeStart() {
        //дата конца раньше даты начала, чтобы попасть в ошибку
        newBookingDto.setStart(date);
        newBookingDto.setEnd(date.minusHours(1));
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Дата окончания бронирования не может быть раньше даты начала",
                    ex.getMessage());
        }
    }

    @Test
    @DisplayName("Валидация дат - дата конца раньше текущей даты")
    public void testValidateEndBeforeNow() {
        //дата начала раньше текущей даты и раньше даты конца, дата конца также раньше текущей, чтобы попасть в ошибку
        //случай целого бронирования в прошлом
        newBookingDto.setStart(date.minusDays(1).minusHours(1));
        newBookingDto.setEnd(date.minusDays(1));
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Дата окончания или начала бронирования не может быть раньше текущей даты",
                    ex.getMessage());
        }
    }

    @Test
    @DisplayName("Валидация дат - дата начала раньше текущей даты")
    public void testValidateStartBeforeNow() {
        //дата начала раньше текущей даты и раньше даты конца, дата конца текущая, чтобы попасть в ошибку
        //случай начала бронирования в прошлом
        newBookingDto.setStart(date.minusDays(1));
        newBookingDto.setEnd(date);
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.save(newBookingDto, savedBooker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Дата окончания или начала бронирования не может быть раньше текущей даты",
                    ex.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление бронирования - одобрение - все хорошо")
    public void testUpdateAllOkApprove() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //одобрили
        BookingDto result = bookingService.update(savedBooking.getId(), savedOwner.getId(), true);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(BookingProcessState.APPROVED, result.getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getEnd());
    }

    @Test
    @DisplayName("Обновление бронирования - отказ - все хорошо")
    public void testUpdateAllOkReject() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //не одобрили
        BookingDto result = bookingService.update(savedBooking.getId(), savedOwner.getId(), false);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(BookingProcessState.REJECTED, result.getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getEnd());
    }

    @Test
    @DisplayName("Обновление бронирования - неверное бронирование")
    public void testUpdateWrongId() {
        UserDto savedOwner = userService.save(owner);
        try {
            bookingService.update(999L, savedOwner.getId(), false);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Бронирование с id = 999 не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление бронирования - неправильный пользователь")
    public void testUpdateWrongOwnerId() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        try {
            bookingService.update(savedBooking.getId(), 999L, false);
        } catch (WrongUserException e) {
            Assertions.assertEquals("Изменять статус бронирования может только владелец вещи", e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск по id от владельца - все хорошо")
    public void testFindByIdAllOkByOwnerId() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //ищем
        BookingDto result = bookingService.findById(savedBooking.getId(), savedOwner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(BookingProcessState.WAITING, result.getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getEnd());
    }

    @Test
    @DisplayName("Поиск по id от букера - все хорошо")
    public void testFindByIdAllOkByBookerId() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //ищем
        BookingDto result = bookingService.findById(savedBooking.getId(), savedBooker.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(BookingProcessState.WAITING, result.getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getEnd());
    }

    @Test
    @DisplayName("Поиск по id - неверный пользователь")
    public void testFindByIdWrongUserId() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        try {
            bookingService.findById(savedBooking.getId(), 999L);
        } catch (WrongUserException e) {
            Assertions.assertEquals("Просматривать бронирование может только владелец вещи или автор бронирования",
                    e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск прошедших бронирований - все хорошо")
    public void testFindPastBookingByBookerIdAndItemId() throws InterruptedException {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        //задержка в 5 сек, чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        bookingService.findPastBookingByBookerIdAndItemId(savedBooker.getId(), savedItem.getId());
    }

    @Test
    @DisplayName("Поиск прошедших бронирований - нет бронирования")
    public void testFindPastBookingByBookerIdAndItemIdNoBooking() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        try {
            bookingService.findPastBookingByBookerIdAndItemId(savedBooker.getId(), savedItem.getId());
        } catch (BadRequestException e) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск прошедших бронирований - бронирование в будущем")
    public void testFindPastBookingByBookerIdAndItemIdBookingInFuture() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронь в будущем
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDtoFuture, savedBooker.getId());
        try {
            bookingService.findPastBookingByBookerIdAndItemId(savedBooker.getId(), savedItem.getId());
        } catch (BadRequestException e) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск броней по вещи и состоянию")
    public void testFindByItemIdAndState() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //ищем в состоянии ожидания
        List<BookingDto> result = bookingService.findByItemIdAndState(savedItem.getId(), BookingProcessState.WAITING);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по вещи и состоянию - нет бронирования")
    public void testFindByItemIdAndStateNoBooking() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        //ищем одобренное
        List<BookingDto> result = bookingService.findByItemIdAndState(savedItem.getId(), BookingProcessState.APPROVED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск броней по списку вещей и состоянию")
    public void testFindAllByItemIdInAndState() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        List<Long> itemIds = new ArrayList<>();
        itemIds.add(savedItem.getId());
        //ищем в состоянии ожидания
        List<BookingDto> result = bookingService.findAllByItemIdInAndState(itemIds, BookingProcessState.WAITING);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по списку вещей и состоянию - нет бронирования")
    public void testFindAllByItemIdInAndStateNoBooking() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        List<Long> itemIds = new ArrayList<>();
        itemIds.add(savedItem.getId());
        //ищем одобренное
        List<BookingDto> result = bookingService.findAllByItemIdInAndState(itemIds, BookingProcessState.APPROVED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - все")
    public void testFindByBookerIdAndStateAll() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //ищем все
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - все - 2 вещи")
    public void testFindByBookerIdAndStateAll2() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), true);
        //ищем все
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - ожидающие")
    public void testFindByBookerIdAndStateWAITING() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), true);
        //ищем в состоянии ожидания - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.WAITING);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - отклоненные")
    public void testFindByBookerIdAndStateREJECTED() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), false);
        //ищем в состоянии отклонено - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.REJECTED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.REJECTED, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking1.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking1.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking1.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking1.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking1.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - будущие")
    public void testFindByBookerIdAndStateFUTURE() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронирование
        newBookingDto.setItemId(savedItem.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //ищем бронь в будущем- это 1из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.FUTURE);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking1.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking1.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking1.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking1.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking1.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - текущие")
    public void testFindByBookerIdAndStateCURRENT() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронирование текущее - закончится завтра
        newBookingDto.setItemId(savedItem.getId());
        newBookingDto.setStart(date)
                .setEnd(date.plusDays(1).plusSeconds(2));
        //бронь в будущем целиком
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //ищем бронь текущую - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.CURRENT);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - прошедшие")
    public void testFindByBookerIdAndStatePAST() throws InterruptedException {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронь в прошлом
        newBookingDto.setItemId(savedItem.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //задержка в 5 сек чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        List<BookingDto> result = bookingService.findByBookerIdAndState(savedBooker.getId(), BookingStateSearch.PAST);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - все")
    public void testFindByOwnerIdAndStateAll() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //ищем все
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - все - 2 вещи")
    public void testFindByOwnerIdAndStateAll2() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), true);
        //ищем все
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - ожидающие")
    public void testFindByOwnerIdAndStateWAITING() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), true);
        //ищем в состоянии ожидания - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.WAITING);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - отклоненные")
    public void testFindByOwnerIdAndStateREJECTED() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        newBookingDto.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.update(savedBooking1.getId(), savedOwner.getId(), false);
        //ищем в состоянии отклонено - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.REJECTED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.REJECTED, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking1.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking1.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking1.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking1.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking1.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - будущие")
    public void testFindByOwnerIdAndStateFUTURE() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронирование
        newBookingDto.setItemId(savedItem.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        bookingService.save(newBookingDto, savedBooker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //ищем бронь в будущем- это 1из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.FUTURE);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking1.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking1.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking1.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking1.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking1.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - текущие")
    public void testFindByOwnerIdAndStateCURRENT() {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронирование текущее - закончится завтра
        newBookingDto.setItemId(savedItem.getId());
        newBookingDto.setStart(date)
                .setEnd(date.plusDays(1).plusSeconds(2));
        //бронь в будущем целиком
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //ищем бронь текущую - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.CURRENT);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - прошедшие")
    public void testFindByOwnerIdAndStatePAST() throws InterruptedException {
        //владелец-вещь-букер-бронирование
        UserDto savedOwner = userService.save(owner);
        item.setUser(UserMapper.mapToUser(savedOwner));
        Item savedItem = itemRepository.save(item);
        //бронь в прошлом
        newBookingDto.setItemId(savedItem.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(savedItem.getId());
        UserDto savedBooker = userService.save(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //задержка в 5 сек чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        List<BookingDto> result = bookingService.findByOwnerIdAndState(savedOwner.getId(), BookingStateSearch.PAST);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getFirst().getStatus());
        Assertions.assertEquals(savedBooking.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedBooking.getItem(), result.getFirst().getItem());
        Assertions.assertEquals(savedBooking.getBooker(), result.getFirst().getBooker());
        Assertions.assertEquals(savedBooking.getStart(), result.getFirst().getStart());
        Assertions.assertEquals(savedBooking.getEnd(), result.getFirst().getEnd());
    }
}