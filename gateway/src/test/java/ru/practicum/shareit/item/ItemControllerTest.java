package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private final NewItemDto newItemDto = new NewItemDto()
            .setName("New Item")
            .setDescription("New Description")
            .setAvailable(true);
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("New Comment");

    @Test
    @DisplayName("Post items - ошибка без названия")
    public void testSaveWithNoName() throws Exception {
        newItemDto.setName(null);
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items - ошибка пустое название")
    public void testSaveWithEmptyName() throws Exception {
        newItemDto.setName("");
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items - ошибка без описания")
    public void testSaveWithNoDescription() throws Exception {
        newItemDto.setDescription(null);
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items - ошибка пустое описания")
    public void testSaveWithEmptyDescription() throws Exception {
        newItemDto.setDescription("");
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items - ошибка без доступности")
    public void testSaveWithNoAvailable() throws Exception {
        newItemDto.setAvailable(null);
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(newItemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items comments - ошибка нет текста")
    public void testSaveCommentWithNoText() throws Exception {
        newCommentDto.setText(null);
        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .content(mapper.writeValueAsString(newCommentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id",1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post items comments - ошибка пустой текст")
    public void testSaveCommentWithEmptyText() throws Exception {
        newCommentDto.setText("");
        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .content(mapper.writeValueAsString(newCommentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }
}
