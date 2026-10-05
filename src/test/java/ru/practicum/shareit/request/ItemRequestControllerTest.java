package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private final ItemDto itemDto = new ItemDto()
            .setId(1L)
            .setName("test")
            .setDescription("test");
    private final List<ItemDto> items = new ArrayList<>();
    private final ItemRequestDto itemRequestDto = new ItemRequestDto()
            .setId(1L)
            .setDescription("test")
            .setCreated(LocalDateTime.now());
    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto()
            .setDescription("test");
    private final GetItemRequestDto getItemRequestDto = new GetItemRequestDto()
            .setId(1L)
            .setCreated(LocalDateTime.now())
            .setDescription("test")
            .setItems(items);
    List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();
    List<GetItemRequestDto> getItemRequestDtoList = new ArrayList<>();

    @Test
    @DisplayName("Post requests")
    public void testSave() throws Exception {
        when(itemRequestService.save(newItemRequestDto, 1L)).thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(newItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(newItemRequestDto.getDescription())));
    }

    @Test
    @DisplayName("Get requests по Id")
    public void testFindGetItemRequestDtoById() throws Exception {
        items.add(itemDto);
        when(itemRequestService.findGetItemRequestDtoById(getItemRequestDto.getId())).thenReturn(getItemRequestDto);
        mockMvc.perform(get("/requests/" + getItemRequestDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(getItemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(getItemRequestDto.getDescription())))
                .andExpect(jsonPath("$.items.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.items.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.items.[0].description", is(getItemRequestDto.getDescription())));
    }

    @Test
    @DisplayName("Get requests по id пользователя")
    public void testFindGetItemRequestDtoByUserId() throws Exception {
        items.add(itemDto);
        getItemRequestDtoList.add(getItemRequestDto);
        when(itemRequestService.findGetItemRequestDtoByUserId(getItemRequestDto.getId())).thenReturn(getItemRequestDtoList);
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(getItemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(getItemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].items.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].items.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].items.[0].description", is(getItemRequestDto.getDescription())));
    }

    @Test
    @DisplayName("Get requests все")
    public void testFindAllItemRequestDto() throws Exception {
        itemRequestDtoList.add(itemRequestDto);
        when(itemRequestService.findAllItemRequestDto()).thenReturn(itemRequestDtoList);
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())));
    }

    @Test
    @DisplayName("Post requests - ошибка null описание")
    public void testPostWithNoEmail() throws Exception {
        newItemRequestDto.setDescription(null);
        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(newItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post requests - ошибка пустой описание")
    public void testPostWithEmptyEmail() throws Exception {
        newItemRequestDto.setDescription("");
        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(newItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get requests по Id - без id")
    public void testFindGetItemRequestDtoByIdWithNoId() throws Exception {
        mockMvc.perform(get("/requests/"))
                .andExpect(status().is5xxServerError());
    }
}