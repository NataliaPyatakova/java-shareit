package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ItemRequestMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final User requester = new User()
            .setId(3L)
            .setName("requester")
            .setEmail("requester@email.ru");
    private final ItemRequest itemRequest = new ItemRequest()
            .setId(1L)
            .setUser(requester)
            .setDescription("request")
            .setDateCreated(date);
    private final ItemRequestDto itemRequestDto = new ItemRequestDto()
            .setId(1L)
            .setDescription("request")
            .setCreated(date);
    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto()
            .setDescription("request");
    private final ItemDto itemDto = new ItemDto()
            .setId(1L)
            .setDescription("item")
            .setName("item")
            .setUserId(1L)
            .setAvailable(true);
    private final List<ItemDto> items = new ArrayList<>();

    @Test
    public void testMapToItemRequestDto() {
        ItemRequestDto result = ItemRequestMapper.mapToItemRequestDto(itemRequest);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(itemRequest.getId(), result.getId());
        Assertions.assertEquals(itemRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(itemRequest.getDateCreated(), result.getCreated());
    }

    @Test
    public void testMapToItemRequest() {
        ItemRequest result = ItemRequestMapper.mapToItemRequest(itemRequestDto, requester);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(itemRequestDto.getId(), result.getId());
        Assertions.assertEquals(requester.getId(), result.getUser().getId());
        Assertions.assertEquals(itemRequestDto.getDescription(), result.getDescription());
    }

    @Test
    public void testMapToItemRequestForCreate() {
        ItemRequest result = ItemRequestMapper.mapToItemRequestForCreate(newItemRequestDto, requester);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(requester.getId(), result.getUser().getId());
        Assertions.assertEquals(newItemRequestDto.getDescription(), result.getDescription());
    }

    @Test
    public void testMapToGetItemRequestDto() {
        items.add(itemDto);
        GetItemRequestDto result = ItemRequestMapper.mapToGetItemRequestDto(itemRequest, items);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(itemRequest.getId(), result.getId());
        Assertions.assertEquals(itemRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(itemRequest.getDateCreated(), result.getCreated());
        Assertions.assertEquals(items, result.getItems());
    }
}