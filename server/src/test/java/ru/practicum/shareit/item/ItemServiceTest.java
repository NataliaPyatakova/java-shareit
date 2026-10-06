package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
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
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private static final long TIMEOUT = 2;
    private final LocalDateTime date = LocalDateTime.now();
    private final NewItemDto newItemDto = new NewItemDto()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);
    private final NewUserDto newUserDto = new NewUserDto()
            .setName("test_user")
            .setEmail("test_user@test.ru");
    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto().setDescription("test");
    private final NewUserDto requester = new NewUserDto()
            .setName("test_requester")
            .setEmail("test_requester@test.ru");
    private final UpdateItemDto updateItemDto = new UpdateItemDto()
            .setName("update_item")
            .setDescription("update description")
            .setAvailable(false);
    private final NewUserDto booker = new NewUserDto()
            .setName("test_booker")
            .setEmail("test_booker@test.ru");
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("test comment");
    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setStart(date)
            .setEnd(date.plusSeconds(2));
    private final NewBookingDto newBookingDtoFuture = new NewBookingDto()
            .setStart(date.plusDays(1))
            .setEnd(date.plusDays(1).plusSeconds(2));

    @Test
    @DisplayName("Сохранение вещи без запроса")
    public void testSaveItemWithNoRequest() {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto result = itemService.save(newItemDto, savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(savedUser.getId(), result.getUserId());
    }

    @Test
    @DisplayName("Сохранение вещи с запросом")
    public void testSaveItemWithRequest() {
        UserDto savedUser = userService.save(newUserDto);
        UserDto savedRequester = userService.save(requester);
        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto,
                UserMapper.mapToUser(savedRequester)));
        newItemDto.setRequestId(request.getId());
        ItemDto result = itemService.save(newItemDto, savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(savedUser.getId(), result.getUserId());
    }

    @Test
    @DisplayName("Обновление вещи - правильный Id")
    public void testUpdateItemWithProperId() {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        ItemDto result = itemService.update(updateItemDto, savedItem.getId(), savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        itemService.save(newItemDto, savedUser.getId());
        try {
            itemService.update(updateItemDto, 999L, savedUser.getId());
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по запросу - правильный id")
    public void testFindAllByProperRequestId() {
        UserDto savedUser = userService.save(newUserDto);
        UserDto savedRequester = userService.save(requester);
        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto,
                UserMapper.mapToUser(savedRequester)));
        newItemDto.setRequestId(request.getId());
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        List<ItemDto> result = itemService.findAllByRequestId(request.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(savedItem.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedItem.getName(), result.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по запросу - нет вещей по запросу")
    public void testFindAllByRequestIdWithNoRequest() {
        UserDto savedRequester = userService.save(requester);
        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto,
                UserMapper.mapToUser(savedRequester)));
        List<ItemDto> result = itemService.findAllByRequestId(request.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        UserDto savedRequester = userService.save(requester);
        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto,
                UserMapper.mapToUser(savedRequester)));
        newItemDto.setRequestId(request.getId());
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        List<Long> itemRequestIds = new ArrayList<>();
        itemRequestIds.add(request.getId());
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(savedItem.getId(), items.getFirst().getId());
        Assertions.assertEquals(savedItem.getName(), items.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск вещей по запросам - 2 вещи по запросу")
    public void testFindAllByItemRequestIdIn2Items() {
        UserDto savedUser = userService.save(newUserDto);
        UserDto savedRequester = userService.save(requester);
        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto,
                UserMapper.mapToUser(savedRequester)));
        newItemDto.setRequestId(request.getId());
        itemService.save(newItemDto, savedUser.getId());
        itemService.save(newItemDto, savedUser.getId());
        List<Long> itemRequestIds = new ArrayList<>();
        itemRequestIds.add(request.getId());
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());
    }

    @Test
    @DisplayName("Сохранение комментария - нет бронирования")
    public void testSaveCommentWithNoBooking() {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        UserDto savedBooker = userService.save(booker);
        try {
            itemService.saveComment(savedBooker.getId(), savedItem.getId(), newCommentDto);
        } catch (BadRequestException ex) {
            Assertions.assertEquals("Бронирование этой вещи данным пользователем не найдено", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение комментария - все хорошо")
    public void testSaveCommentWithBooking() throws InterruptedException {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        UserDto savedBooker = userService.save(booker);
        newBookingDto.setItemId(savedItem.getId());
        bookingService.save(newBookingDto, savedBooker.getId());
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        CommentDto result = itemService.saveComment(savedBooker.getId(), savedItem.getId(), newCommentDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCommentDto.getText(), result.getText());
        Assertions.assertEquals(savedBooker.getName(), result.getAuthorName());
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
        UserDto savedUser = userService.save(newUserDto);
        ItemDto result = itemService.save(newItemDto, savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        ItemDto result = itemService.save(newItemDto, savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        itemService.save(newItemDto, savedUser.getId());
        try {
            itemService.findGetItemDtoByItemId(999L, 999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Вещь с id = 999 не найдена", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по id - не от владельца, без комментариев")
    public void testFindGetItemDtoByItemIdAllOk() {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        GetItemDto result = itemService.findGetItemDtoByItemId(savedItem.getId(), savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        UserDto savedBooker = userService.save(booker);
        //прошлое бронирование
        newBookingDto.setItemId(savedItem.getId());
        //будущее бронирование
        newBookingDtoFuture.setItemId(savedItem.getId());
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //одобряем бронь - иначе не увидим ее в датах
        bookingService.update(savedBooking.getId(), savedUser.getId(), true);
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //одобряем бронь - иначе не увидим ее в датах
        bookingService.update(savedBooking1.getId(), savedUser.getId(), true);
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        //добавялем комментарий
        CommentDto savedComment = itemService.saveComment(savedBooker.getId(), savedItem.getId(), newCommentDto);
        GetItemDto result = itemService.findGetItemDtoByItemId(savedItem.getId(), savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getAvailable());
        Assertions.assertEquals(newBookingDto.getEnd(), result.getLastBooking());
        Assertions.assertEquals(newBookingDtoFuture.getStart(), result.getNextBooking());
        Assertions.assertEquals(1, result.getComments().size());
        Assertions.assertEquals(savedComment.getId(), result.getComments().getFirst().getId());
        Assertions.assertEquals(savedComment.getText(), result.getComments().getFirst().getText());
    }

    @Test
    @DisplayName("Поиск вещей по владельцу - неверный владелец")
    public void testFindAllByUserIdWrongUserId() {
        UserDto savedUser = userService.save(newUserDto);
        itemService.save(newItemDto, savedUser.getId());
        try {
            itemService.findAllByUserId(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск вещей по владельцу - без комментариев и бронирования")
    public void testFindAllByUserIdNoBooking() {
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        List<GetItemDto> result = itemService.findAllByUserId(savedUser.getId());
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
        UserDto savedUser = userService.save(newUserDto);
        ItemDto savedItem = itemService.save(newItemDto, savedUser.getId());
        UserDto savedBooker = userService.save(booker);
        //прошлое бронирование
        newBookingDto.setItemId(savedItem.getId());
        //будущее бронирование
        newBookingDtoFuture.setItemId(savedItem.getId());
        BookingDto savedBooking = bookingService.save(newBookingDto, savedBooker.getId());
        //одобряем бронь - иначе не увидим ее в датах
        bookingService.update(savedBooking.getId(), savedUser.getId(), true);
        BookingDto savedBooking1 = bookingService.save(newBookingDtoFuture, savedBooker.getId());
        //одобряем бронь - иначе не увидим ее в датах
        bookingService.update(savedBooking1.getId(), savedUser.getId(), true);
        //задержка в 5 сек чтобы бронирование стало прошлым
        TimeUnit.SECONDS.sleep(TIMEOUT);
        //добавялем комментарий
        CommentDto savedComment = itemService.saveComment(savedBooker.getId(), savedItem.getId(), newCommentDto);
        List<GetItemDto> result = itemService.findAllByUserId(savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedItem.getId(), result.getFirst().getId());
        Assertions.assertEquals(savedItem.getUserId(), result.getFirst().getUserId());
        Assertions.assertEquals(savedItem.getName(), result.getFirst().getName());
        Assertions.assertEquals(savedItem.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(savedItem.getAvailable(), result.getFirst().getAvailable());
        Assertions.assertEquals(newBookingDto.getEnd(), result.getFirst().getLastBooking());
        Assertions.assertEquals(newBookingDtoFuture.getStart(), result.getFirst().getNextBooking());
        Assertions.assertEquals(1, result.getFirst().getComments().size());
        Assertions.assertEquals(savedComment.getId(), result.getFirst().getComments().getFirst().getId());
        Assertions.assertEquals(savedComment.getText(), result.getFirst().getComments().getFirst().getText());
    }
}