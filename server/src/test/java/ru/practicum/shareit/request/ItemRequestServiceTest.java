package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;


@Transactional
@SpringBootTest
public class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto().setDescription("test");
    private final NewUserDto newUserDto = new NewUserDto().setName("test_user").setEmail("test_user@test.ru");
    private final NewUserDto owner = new NewUserDto().setName("test_owner").setEmail("test_owner@test.ru");
    private final NewItemDto newItemDtoWithReq = new NewItemDto()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);

    @Test
    @DisplayName("Поиск всех запросов - пустой список")
    public void testFindAllItemRequestDtoWithNoRequests() {
        List<ItemRequestDto> listItemRequestDto = itemRequestService.findAllItemRequestDto();
        Assertions.assertNotNull(listItemRequestDto);
        Assertions.assertEquals(0, listItemRequestDto.size());
    }

    @Test
    @DisplayName("Поиск всех запросов - 1 запрос")
    public void testFindAllItemRequestDtoWith1Request() {
        UserDto savedUser = userService.save(newUserDto);
        ItemRequestDto request = itemRequestService.save(newItemRequestDto, savedUser.getId());
        List<ItemRequestDto> result = itemRequestService.findAllItemRequestDto();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(request.getId(), result.getFirst().getId());
        Assertions.assertEquals(request.getDescription(), result.getFirst().getDescription());
    }

    @Test
    @DisplayName("Поиск запроса по id - правильный id")
    public void testFindGetItemRequestDtoByProperId() {
        UserDto savedUser = userService.save(newUserDto);
        ItemRequestDto savedRequest = itemRequestService.save(newItemRequestDto, savedUser.getId());
        UserDto savedOwner = userService.save(owner);
        newItemDtoWithReq.setRequestId(savedRequest.getId());
        ItemDto savedItem = itemService.save(newItemDtoWithReq, savedOwner.getId());
        GetItemRequestDto result = itemRequestService.findGetItemRequestDtoById(savedRequest.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedRequest.getId(), result.getId());
        Assertions.assertEquals(savedRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(savedItem.getId(), result.getItems().getFirst().getId());
        Assertions.assertEquals(savedItem.getName(), result.getItems().getFirst().getName());
    }

    @Test
    @DisplayName("Поиск запроса по id - неправильный id")
    public void testFindGetItemRequestDtoByWrongId() {
        UserDto savedUser = userService.save(newUserDto);
        itemRequestService.save(newItemRequestDto, savedUser.getId());
        try {
            itemRequestService.findGetItemRequestDtoById(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Запрос с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск запросов по пользователю - правильный id")
    public void testFindGetItemRequestDtoByProperUserId() {
        UserDto savedUser = userService.save(newUserDto);
        itemRequestService.save(newItemRequestDto, savedUser.getId());
        List<GetItemRequestDto> result = itemRequestService.findGetItemRequestDtoByUserId(savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Поиск запросов по пользователю - правильный id - нет запросов")
    public void testFindGetItemRequestDtoByProperUserIdWithNoRequests() {
        UserDto savedUser = userService.save(newUserDto);
        List<GetItemRequestDto> result = itemRequestService.findGetItemRequestDtoByUserId(savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }
}