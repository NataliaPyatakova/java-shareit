package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper mapper;

    private final NewUserDto newUserDto = new NewUserDto().setName("New User").setEmail("new_user@email.ru");
    private final UpdateUserDto updateUserDto = new UpdateUserDto().setName("Update User").setEmail("update_user@email.ru");

    @Test
    @DisplayName("Post users - ошибка null email")
    public void testPostWithNoEmail() throws Exception {
        newUserDto.setEmail(null);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post users - ошибка пустой email")
    public void testPostWithEmptyEmail() throws Exception {
        newUserDto.setEmail("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post users - ошибка не email")
    public void testPostWithNotEmail() throws Exception {
        newUserDto.setEmail("test");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch users - ошибка не email")
    public void testPatchWithNotEmail() throws Exception {
        updateUserDto.setEmail("test");
        mockMvc.perform(patch("/users/" + 1)
                        .content(mapper.writeValueAsString(updateUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch users по id - без Id")
    public void testUpdateWithNoId() throws Exception {
        mockMvc.perform(patch("/users/")
                        .content(mapper.writeValueAsString(updateUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}