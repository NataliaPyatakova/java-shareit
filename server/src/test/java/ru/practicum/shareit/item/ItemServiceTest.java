package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;
    private final EntityManager em;

    private static final long TIMEOUT = 2;
    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = new User()
            .setName("test_user")
            .setEmail("test_user@test.ru");
    private final User requester = new User()
            .setName("test_requester")
            .setEmail("test_requester@test.ru");
    private final User booker = new User()
            .setName("test_booker")
            .setEmail("test_booker@test.ru");
    private final ItemRequest itemRequest = new ItemRequest()
            .setDescription("test")
            .setDateCreated(date);
    private final Booking newBooking = new Booking()
            .setStart(date)
            .setEnd(date.plusSeconds(2))
            .setState(BookingProcessState.WAITING);
    private final Booking newBookingFuture = new Booking()
            .setStart(date.plusDays(1))
            .setEnd(date.plusDays(1).plusSeconds(2))
            .setState(BookingProcessState.WAITING);
    private final NewItemDto newItemDto = new NewItemDto()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);
    private final UpdateItemDto updateItemDto = new UpdateItemDto()
            .setName("update_item")
            .setDescription("update description")
            .setAvailable(false);
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("test comment");

    @Test
    @DisplayName("Сохранение вещи без запроса")
    public void testSaveItemWithNoRequest() {
        em.persist(owner);
        ItemDto result = itemService.save(newItemDto, owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(owner.getId(), result.getUserId());
    }

    @Test
    @DisplayName("Сохранение вещи с запросом")
    public void testSaveItemWithRequest() {
        em.persist(owner);
        em.persist(requester);
        itemRequest.setUser(requester);
        em.persist(itemRequest);
        newItemDto.setRequestId(itemRequest.getId());
        ItemDto result = itemService.save(newItemDto, owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(owner.getId(), result.getUserId());
    }

    @Test
    @DisplayName("Обновление вещи - правильный Id")
    public void testUpdateItemWithProperId() {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        ItemDto result = itemService.update(updateItemDto, savedItem.getId(), owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getUserId());
        Assertions.assertEquals(updateItemDto.getName(), result.getName());
        Assertions.assertEquals(updateItemDto.getDescription(), result.getDescription());
        Assertions.assertEquals(updateItemDto.getAvailable(), result.getAvailable());
    }

    @Test
    @DisplayName("Обновление вещи - неправильный Id")
    public void testUpdateItemWithWrongId() {
        em.persist(owner);
        itemService.save(newItemDto, owner.getId());
        try {
            itemService.update(updateItemDto, 999L, owner.getId());
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по запросу - правильный id")
    public void testFindAllByProperRequestId() {
        em.persist(owner);
        em.persist(requester);
        itemRequest.setUser(requester);
        em.persist(itemRequest);
        newItemDto.setRequestId(itemRequest.getId());
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        List<ItemDto> result = itemService.findAllByRequestId(itemRequest.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedItem.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedItem.getName(), result.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по запросу - нет вещей по запросу")
    public void testFindAllByRequestIdWithNoRequest() {
        em.persist(requester);
        itemRequest.setUser(requester);
        em.persist(itemRequest);
        List<ItemDto> result = itemService.findAllByRequestId(itemRequest.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск вещей по запросу - неправильный id")
    public void testFindAllByRequestIdWithWrongRequest() {
        try {
            itemService.findAllByRequestId(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Запрос с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по запросам - нет вещей по запросам")
    public void testFindAllByItemRequestIdInNoItems() {
        List<Long> itemRequestIds = new ArrayList<>();
        itemRequestIds.add(1L);
        itemRequestIds.add(2L);
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(0, items.size());
    }

    @Test
    @DisplayName("Поиск вещей по запросам - 1 вещь по запросу")
    public void testFindAllByItemRequestIdIn1Items() {
        em.persist(owner);
        em.persist(requester);
        itemRequest.setUser(requester);
        em.persist(itemRequest);
        newItemDto.setRequestId(itemRequest.getId());
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        List<Long> itemRequestIds = new ArrayList<>();
        itemRequestIds.add(itemRequest.getId());
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(savedItem.getId(), items.getFirst().getId());
        Assertions.assertEquals(savedItem.getName(), items.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по запросам - 2 вещи по запросу")
    public void testFindAllByItemRequestIdIn2Items() {
        em.persist(owner);
        em.persist(requester);
        itemRequest.setUser(requester);
        em.persist(itemRequest);
        newItemDto.setRequestId(itemRequest.getId());
        itemService.save(newItemDto, owner.getId());
        itemService.save(newItemDto, owner.getId());
        List<Long> itemRequestIds = new ArrayList<>();
        itemRequestIds.add(itemRequest.getId());
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());
    }

    @Test
    @DisplayName("Сохранение комментария - нет бронирования")
    public void testSaveCommentWithNoBooking() {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        em.persist(booker);
        try {
            itemService.saveComment(booker.getId(), savedItem.getId(), newCommentDto);
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение комментария - все хорошо")
    public void testSaveCommentWithBooking() throws InterruptedException {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        em.persist(booker);
        newBooking.setBooker(booker);
        newBooking.setItem(ItemMapper.mapToItem(savedItem, owner));
        em.persist(newBooking);
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        CommentDto result = itemService.saveComment(booker.getId(), savedItem.getId(), newCommentDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCommentDto.getText(), result.getText());
        Assertions.assertEquals(booker.getName(), result.getAuthorName());
        Assertions.assertEquals(date.toLocalDate(), result.getCreated().toLocalDate());
    }

    @Test
    @DisplayName("Поиск вещей по названию или описанию - null")
    public void testSearchNoText() {
        List<ItemDto> items = itemService.search(null);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(0, items.size());
    }

    @Test
    @DisplayName("Поиск вещей по названию или описанию - пустой")
    public void testSearchEmptyText() {
        List<ItemDto> items = itemService.search("");
        Assertions.assertNotNull(items);
        Assertions.assertEquals(0, items.size());
    }

    @Test
    @DisplayName("Поиск вещей по названию или описанию - по названию")
    public void testSearchTextByName() {
        em.persist(owner);
        ItemDto result = itemService.save(newItemDto, owner.getId());
        System.out.println(result);
        List<ItemDto> items = itemService.search("test_item");
        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(result.getId(), items.getFirst().getId());
        Assertions.assertEquals(result.getName(), items.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по названию или описанию - по описанию")
    public void testSearchTexByDescription() {
        em.persist(owner);
        ItemDto result = itemService.save(newItemDto, owner.getId());
        System.out.println(result);
        List<ItemDto> items = itemService.search("test description");
        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(result.getId(), items.getFirst().getId());
        Assertions.assertEquals(result.getName(), items.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по id - неправильный id")
    public void testFindGetItemDtoByItemIdWrongId() {
        em.persist(owner);
        itemService.save(newItemDto, owner.getId());
        try {
            itemService.findGetItemDtoByItemId(999L, 999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по id - не от владельца, без комментариев")
    public void testFindGetItemDtoByItemIdAllOk() {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        GetItemDto result = itemService.findGetItemDtoByItemId(savedItem.getId(), 999L);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getAvailable());
        Assertions.assertNull(result.getLastBooking());
        Assertions.assertNull(result.getNextBooking());
        Assertions.assertEquals(0, result.getComments().size());
    }

    @Test
    @DisplayName("Поиск вещей по id - от владельца, без комментариев и бронирования")
    public void testFindGetItemDtoByItemIdByOwnerNoBooking() {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        GetItemDto result = itemService.findGetItemDtoByItemId(savedItem.getId(), owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getAvailable());
        Assertions.assertNull(result.getLastBooking());
        Assertions.assertNull(result.getNextBooking());
        Assertions.assertEquals(0, result.getComments().size());
    }

    @Test
    @DisplayName("Поиск вещей по id - от владельца, с комментариями и с бронированием")
    public void testFindGetItemDtoByItemIdByOwnerBookingNoComments() throws InterruptedException {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        em.persist(booker);
        //прошлое бронирование
        newBooking.setBooker(booker);
        newBooking.setItem(ItemMapper.mapToItem(savedItem, owner));
        newBooking.setState(BookingProcessState.APPROVED); //одобряем бронь - иначе не увидим ее в датах
        em.persist(newBooking);
        //будущее бронирование
        newBookingFuture.setBooker(booker);
        newBookingFuture.setItem(ItemMapper.mapToItem(savedItem, owner));
        newBookingFuture.setState(BookingProcessState.APPROVED); //одобряем бронь - иначе не увидим ее в датах
        em.persist(newBookingFuture);
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        //добавялем комментарий
        CommentDto savedComment = itemService.saveComment(booker.getId(), savedItem.getId(), newCommentDto);
        GetItemDto result = itemService.findGetItemDtoByItemId(savedItem.getId(), owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getAvailable());
        Assertions.assertEquals(newBooking.getEnd(), result.getLastBooking());
        Assertions.assertEquals(newBookingFuture.getStart(), result.getNextBooking());
        Assertions.assertEquals(1, result.getComments().size());
        Assertions.assertEquals(savedComment.getId(), result.getComments().getFirst().getId());
        Assertions.assertEquals(savedComment.getText(), result.getComments().getFirst().getText());
    }

    @Test
    @DisplayName("Поиск вещей по владельцу - неверный владелец")
    public void testFindAllByUserIdWrongUserId() {
        em.persist(owner);
        itemService.save(newItemDto, owner.getId());
        try {
            itemService.findAllByUserId(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по владельцу - без комментариев и бронирования")
    public void testFindAllByUserIdNoBooking() {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        List<GetItemDto> result = itemService.findAllByUserId(owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getFirst().getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getFirst().getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getFirst().getAvailable());
        Assertions.assertNull(result.getFirst().getLastBooking());
        Assertions.assertNull(result.getFirst().getNextBooking());
        Assertions.assertEquals(0, result.getFirst().getComments().size());
    }

    @Test
    @DisplayName("Поиск вещей по владельцу - с комментариями и с бронированием")
    public void testFindAllByUserId() throws InterruptedException {
        em.persist(owner);
        ItemDto savedItem = itemService.save(newItemDto, owner.getId());
        em.persist(booker);
        //прошлое бронирование
        newBooking.setBooker(booker);
        newBooking.setItem(ItemMapper.mapToItem(savedItem, owner));
        newBooking.setState(BookingProcessState.APPROVED); //одобряем бронь - иначе не увидим ее в датах
        em.persist(newBooking);
        //будущее бронирование
        newBookingFuture.setBooker(booker);
        newBookingFuture.setItem(ItemMapper.mapToItem(savedItem, owner));
        newBookingFuture.setState(BookingProcessState.APPROVED); //одобряем бронь - иначе не увидим ее в датах
        em.persist(newBookingFuture);
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        //добавялем комментарий
        CommentDto savedComment = itemService.saveComment(booker.getId(), savedItem.getId(), newCommentDto);
        List<GetItemDto> result = itemService.findAllByUserId(owner.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getFirst().getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getFirst().getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getFirst().getAvailable());
        Assertions.assertEquals(newBooking.getEnd(), result.getFirst().getLastBooking());
        Assertions.assertEquals(newBookingFuture.getStart(), result.getFirst().getNextBooking());
        Assertions.assertEquals(1, result.getFirst().getComments().size());
        Assertions.assertEquals(savedComment.getId(), result.getFirst().getComments().getFirst().getId());
        Assertions.assertEquals(savedComment.getText(), result.getFirst().getComments().getFirst().getText());
    }
}