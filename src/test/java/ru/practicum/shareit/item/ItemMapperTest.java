package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.GetItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ItemMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final LocalDateTime date1 = LocalDateTime.MAX;
    private final User user = new User()
            .setId(1L)
            .setName("user")
            .setEmail("email@email.ru");
    private final Item item = new Item()
            .setId(1L)
            .setName("item")
            .setDescription("test")
            .setAvailable(true)
            .setUser(user);
    private final User commentator = new User()
            .setId(2L)
            .setName("commentator")
            .setEmail("commentator@email.ru");
    private final User requester = new User()
            .setId(3L)
            .setName("requester")
            .setEmail("requester@email.ru");
    private final Comment comment = new Comment()
            .setId(1L)
            .setUser(commentator)
            .setItem(item)
            .setText("test")
            .setDateCreated(date);
    List<Comment> listComments = new ArrayList<>();
    private final NewItemDto newItemDto = new NewItemDto()
            .setName("item")
            .setDescription("test")
            .setAvailable(true);
    private final ItemRequest itemRequest = new ItemRequest()
            .setId(1L)
            .setUser(requester)
            .setDescription("request")
            .setDateCreated(date);

    @Test
    public void testMapToItemDto() {
        ItemDto result = ItemMapper.mapToItemDto(item);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item.getId(), result.getId());
        Assertions.assertEquals(item.getUser().getId(), result.getUserId());
        Assertions.assertEquals(item.getName(), result.getName());
        Assertions.assertEquals(item.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getAvailable(), result.getAvailable());
    }

    @Test
    public void testMapToGetItemDto() {
        listComments.add(comment);
        GetItemDto result = ItemMapper.mapToGetItemDto(item, listComments, date, date1);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item.getId(), result.getId());
        Assertions.assertEquals(item.getUser().getId(), result.getUserId());
        Assertions.assertEquals(item.getName(), result.getName());
        Assertions.assertEquals(item.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getAvailable(), result.getAvailable());
        Assertions.assertEquals(listComments.size(), result.getComments().size());
        Assertions.assertEquals(listComments.getFirst().getText(), result.getComments().getFirst().getText());
        Assertions.assertEquals(date, result.getLastBooking());
        Assertions.assertEquals(date1, result.getNextBooking());
    }

    @Test
    public void testMapToItemForCreate() {
        Item result = ItemMapper.mapToItemForCreate(newItemDto, user, itemRequest);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user, result.getUser());
        Assertions.assertEquals(item.getName(), result.getName());
        Assertions.assertEquals(item.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getAvailable(), result.getAvailable());
        Assertions.assertEquals(itemRequest, result.getItemRequest());
    }

    @Test
    public void testMapToItemForUpdateWithName() {
        UpdateItemDto updatedItemDto = new UpdateItemDto().setName("updated name");
        Item result = ItemMapper.mapToItemForUpdate(updatedItemDto, item);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item.getId(), result.getId());
        Assertions.assertEquals(item.getUser(), result.getUser());
        Assertions.assertEquals(updatedItemDto.getName(), result.getName());
        Assertions.assertEquals(item.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getAvailable(), result.getAvailable());
    }

    @Test
    public void testMapToItemForUpdateWithDescription() {
        UpdateItemDto updatedItemDto = new UpdateItemDto().setDescription("updated description");
        Item result = ItemMapper.mapToItemForUpdate(updatedItemDto, item);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item.getId(), result.getId());
        Assertions.assertEquals(item.getUser(), result.getUser());
        Assertions.assertEquals(item.getName(), result.getName());
        Assertions.assertEquals(updatedItemDto.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getAvailable(), result.getAvailable());
    }

    @Test
    public void testMapToItemForUpdateWithAvailable() {
        UpdateItemDto updatedItemDto = new UpdateItemDto().setAvailable(false);
        Item result = ItemMapper.mapToItemForUpdate(updatedItemDto, item);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item.getId(), result.getId());
        Assertions.assertEquals(item.getUser(), result.getUser());
        Assertions.assertEquals(item.getName(), result.getName());
        Assertions.assertEquals(item.getDescription(), result.getDescription());
        Assertions.assertEquals(updatedItemDto.getAvailable(), result.getAvailable());
    }
}
