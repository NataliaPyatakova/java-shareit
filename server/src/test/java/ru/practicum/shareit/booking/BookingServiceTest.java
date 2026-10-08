package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    private final EntityManager em;

    private static final long TIMEOUT = 2;
    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = new User()
            .setName("test_user")
            .setEmail("test_user@test.ru");
    private final Item item = new Item()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);
    private final User booker = new User()
            .setName("test_booker")
            .setEmail("test_booker@test.ru");
    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setStart(date)
            .setEnd(date.plusSeconds(2));
    private final NewBookingDto newBookingDtoFuture = new NewBookingDto()
            .setStart(date.plusDays(1))
            .setEnd(date.plusDays(1).plusSeconds(2));

    @Test
    @DisplayName("Сохранение бронирования - все хорошо")
    public void testSaveAllOk() {
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto result = bookingService.save(newBookingDto, booker.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newBookingDto.getItemId(), result.getItem().getId());
        Assertions.assertEquals(booker.getId(), result.getBooker().getId());
        Assertions.assertEquals(newBookingDto.getStart(), result.getStart());
        Assertions.assertEquals(newBookingDto.getEnd(), result.getEnd());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getStatus());
    }

    @Test
    @DisplayName("Сохранение бронирования - несуществующий пользователь")
    public void testSaveWrongUser() {
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
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
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение бронирования - вещь занята")
    public void testSaveNotAvailable() {
        em.persist(owner);
        item.setUser(owner);
        item.setAvailable(false);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
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
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
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
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
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
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
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
        em.persist(booker);
        try {
            bookingService.save(newBookingDto, booker.getId());
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Дата окончания или начала бронирования не может быть раньше текущей даты",
                    ex.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление бронирования - одобрение - все хорошо")
    public void testUpdateAllOkApprove() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //одобрили
        BookingDto result = bookingService.update(savedBooking.getId(), owner.getId(), true);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //не одобрили
        BookingDto result = bookingService.update(savedBooking.getId(), owner.getId(), false);
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
        em.persist(owner);
        try {
            bookingService.update(999L, owner.getId(), false);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Бронирование с id = 999 не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление бронирования - неправильный пользователь")
    public void testUpdateWrongOwnerId() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //ищем
        BookingDto result = bookingService.findById(savedBooking.getId(), owner.getId());
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //ищем
        BookingDto result = bookingService.findById(savedBooking.getId(), booker.getId());
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        //задержка в 5 сек, чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        bookingService.findPastBookingByBookerIdAndItemId(booker.getId(), item.getId());
    }

    @Test
    @DisplayName("Поиск прошедших бронирований - нет бронирования")
    public void testFindPastBookingByBookerIdAndItemIdNoBooking() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        try {
            bookingService.findPastBookingByBookerIdAndItemId(booker.getId(), item.getId());
        } catch (BadRequestException e) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск прошедших бронирований - бронирование в будущем")
    public void testFindPastBookingByBookerIdAndItemIdBookingInFuture() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронь в будущем
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDtoFuture, booker.getId());
        try {
            bookingService.findPastBookingByBookerIdAndItemId(booker.getId(), item.getId());
        } catch (BadRequestException e) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", e.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск броней по вещи и состоянию")
    public void testFindByItemIdAndState() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //ищем в состоянии ожидания
        List<BookingDto> result = bookingService.findByItemIdAndState(item.getId(), BookingProcessState.WAITING);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        //ищем одобренное
        List<BookingDto> result = bookingService.findByItemIdAndState(item.getId(), BookingProcessState.APPROVED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск броней по списку вещей и состоянию")
    public void testFindAllByItemIdInAndState() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        List<Long> itemIds = new ArrayList<>();
        itemIds.add(item.getId());
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        List<Long> itemIds = new ArrayList<>();
        itemIds.add(item.getId());
        //ищем одобренное
        List<BookingDto> result = bookingService.findAllByItemIdInAndState(itemIds, BookingProcessState.APPROVED);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - все")
    public void testFindByBookerIdAndStateAll() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //ищем все
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.ALL);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), true);
        //ищем все
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск броней по букеру и состоянию - ожидающие")
    public void testFindByBookerIdAndStateWAITING() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), true);
        //ищем в состоянии ожидания - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.WAITING);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), false);
        //ищем в состоянии отклонено - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.REJECTED);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронирование
        newBookingDto.setItemId(item.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, booker.getId());
        //ищем бронь в будущем- это 1из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.FUTURE);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронирование текущее - закончится завтра
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(date)
                .setEnd(date.plusDays(1).plusSeconds(2));
        //бронь в будущем целиком
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        bookingService.save(newBookingDtoFuture, booker.getId());
        //ищем бронь текущую - это 1 из 2
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.CURRENT);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронь в прошлом
        newBookingDto.setItemId(item.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        bookingService.save(newBookingDtoFuture, booker.getId());
        //задержка в 5 сек чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        List<BookingDto> result = bookingService.findByBookerIdAndState(booker.getId(), BookingStateSearch.PAST);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        //ищем все
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.ALL);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), true);
        //ищем все
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.ALL);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск броней по владельцу и состоянию - ожидающие")
    public void testFindByOwnerIdAndStateWAITING() {
        //владелец-вещь-букер-бронирование
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), true);
        //ищем в состоянии ожидания - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.WAITING);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        newBookingDto.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDto, booker.getId());
        bookingService.update(savedBooking1.getId(), owner.getId(), false);
        //ищем в состоянии отклонено - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.REJECTED);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронирование
        newBookingDto.setItemId(item.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        bookingService.save(newBookingDto, booker.getId());
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, booker.getId());
        //ищем бронь в будущем- это 1из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.FUTURE);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронирование текущее - закончится завтра
        newBookingDto.setItemId(item.getId());
        newBookingDto.setStart(date)
                .setEnd(date.plusDays(1).plusSeconds(2));
        //бронь в будущем целиком
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        bookingService.save(newBookingDtoFuture, booker.getId());
        //ищем бронь текущую - это 1 из 2
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.CURRENT);
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
        em.persist(owner);
        item.setUser(owner);
        em.persist(item);
        //бронь в прошлом
        newBookingDto.setItemId(item.getId());
        //бронь в будущем
        newBookingDtoFuture.setItemId(item.getId());
        em.persist(booker);
        BookingDto savedBooking = bookingService.save(newBookingDto, booker.getId());
        bookingService.save(newBookingDtoFuture, booker.getId());
        //задержка в 5 сек чтобы найти бронь в прошлом
        TimeUnit.SECONDS.sleep(TIMEOUT);
        List<BookingDto> result = bookingService.findByOwnerIdAndState(owner.getId(), BookingStateSearch.PAST);
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