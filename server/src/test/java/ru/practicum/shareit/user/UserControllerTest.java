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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    private final UserDto userDto = new UserDto().setId(1L).setName("New User").setEmail("new_user@email.ru");
    private final NewUserDto newUserDto = new NewUserDto().setName("New User").setEmail("new_user@email.ru");
    private final UpdateUserDto updateUserDto = new UpdateUserDto().setName("Update User").setEmail("update_user@email.ru");
    private final UserDto userDto1 = new UserDto().setId(2L).setName("Update User").setEmail("update_user@email.ru");
    private final List<UserDto> userDtoList = new ArrayList<>();

    @Test
    @DisplayName("Get users с пустым списком")
    public void testFindAllEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("[]"));

    }

    @Test
    @DisplayName("Get users списком")
    public void testFindAll() throws Exception {
        userDtoList.add(userDto);
        when(userService.findAll()).thenReturn(userDtoList);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())));

    }

    @Test
    @DisplayName("Post users")
    public void testSave() throws Exception {
        when(userService.save(any())).thenReturn(userDto);
        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

    }

    @Test
    @DisplayName("Get users по id")
    public void testFindById() throws Exception {
        when(userService.findById(userDto.getId())).thenReturn(userDto);
        mockMvc.perform(get("/users/" + userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    @DisplayName("Patch users по id")
    public void testUpdate() throws Exception {
        when(userService.update(userDto1.getId(), updateUserDto)).thenReturn(userDto1);
        mockMvc.perform(patch("/users/" + userDto1.getId())
                        .content(mapper.writeValueAsString(updateUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(userDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto1.getName())))
                .andExpect(jsonPath("$.email", is(userDto1.getEmail())));
    }

    @Test
    @DisplayName("Delete users по id")
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/users/" + userDto.getId()))
                .andExpect(status().isNoContent());
    }
}
