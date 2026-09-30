package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    private final UserDto user = new UserDto()
            .setId(1L)
            .setName("John")
            .setEmail("john@email.ru");
    private final NewItemDto newItemDto = new NewItemDto()
            .setName("New Item")
            .setDescription("New Description")
            .setAvailable(true);
    private final ItemDto itemDto = new ItemDto()
            .setId(1L)
            .setUserId(user.getId())
            .setName("New Item")
            .setDescription("New Description")
            .setAvailable(true);
    private final UpdateItemDto updateItemDto = new UpdateItemDto()
            .setName("Updated Name")
            .setDescription("Updated Description")
            .setAvailable(false);
    private final ItemDto updatetedItemDto = new ItemDto()
            .setId(1L)
            .setUserId(user.getId())
            .setName("Updated Name")
            .setDescription("Updated Description")
            .setAvailable(false);
    private final List<CommentDto> commentDtoList = new ArrayList<>();
    private final GetItemDto getItemDto = new GetItemDto()
            .setId(1L)
            .setUserId(user.getId())
            .setName("New Item")
            .setDescription("New Description")
            .setLastBooking(LocalDateTime.now())
            .setNextBooking(LocalDateTime.now())
            .setComments(commentDtoList)
            .setAvailable(false);
    private final List<GetItemDto> getItemDtoList = new ArrayList<>();
    private final List<ItemDto> itemDtoList = new ArrayList<>();
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("New Comment");
    private final CommentDto commentDto = new CommentDto()
            .setId(1L)
            .setText("New Comment")
            .setCreated(LocalDateTime.now())
            .setAuthorName(user.getName());

    @Test
    public void testSave() throws Exception {
        when(itemService.save(newItemDto, user.getId())).thenReturn(itemDto);
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.userId", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(newItemDto.getName())))
                .andExpect(jsonPath("$.description", is(newItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(newItemDto.getAvailable())));
    }

    @Test
    public void testUpdate() throws Exception {
        when(itemService.update(updateItemDto, updatetedItemDto.getId(), updatetedItemDto.getUserId()))
                .thenReturn(updatetedItemDto);
        mockMvc.perform(patch("/items/" + updatetedItemDto.getId())
                        .content(mapper.writeValueAsString(updateItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", updatetedItemDto.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatetedItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.userId", is(updatetedItemDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updateItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(updateItemDto.getAvailable())));
    }

    @Test
    public void testFindByItemId() throws Exception {
        commentDtoList.add(commentDto);
        when(itemService.findGetItemDtoByItemId(getItemDto.getId(), user.getId())).thenReturn(getItemDto);
        mockMvc.perform(get("/items/" + getItemDto.getId())
                        .header("X-Sharer-User-Id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(getItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.userId", is(getItemDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.name", is(getItemDto.getName())))
                .andExpect(jsonPath("$.description", is(getItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(getItemDto.getAvailable())))
                .andExpect(jsonPath("$.comments.[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments.[0].text", is(commentDto.getText())));

    }

    @Test
    public void testFindAllByUserId() throws Exception {
        commentDtoList.add(commentDto);
        getItemDtoList.add(getItemDto);
        when(itemService.findAllByUserId(user.getId())).thenReturn(getItemDtoList);
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(getItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].userId", is(getItemDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(getItemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(getItemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(getItemDto.getAvailable())))
                .andExpect(jsonPath("$.[0].comments.[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].comments.[0].text", is(commentDto.getText())));
    }

    @Test
    public void testSearch() throws Exception {
        commentDtoList.add(commentDto);
        itemDtoList.add(itemDto);
        when(itemService.search(anyString())).thenReturn(itemDtoList);
        mockMvc.perform(get("/items/search")
                        .param("text", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].userId", is(itemDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));
    }


    @Test
    public void testSaveComment() throws Exception {
        when(itemService.saveComment(user.getId(), itemDto.getId(), newCommentDto)).thenReturn(commentDto);
        mockMvc.perform(post("/items/" + itemDto.getId() + "/comment")
                        .content(mapper.writeValueAsString(newCommentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(user.getName())));
    }
}