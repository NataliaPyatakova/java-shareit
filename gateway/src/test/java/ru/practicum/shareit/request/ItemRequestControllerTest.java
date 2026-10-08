package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestService;

    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto()
            .setDescription("test");

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
                .andExpect(status().isNotFound());
    }
}