package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Transactional
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private final NewUserDto newUserDto = new NewUserDto().setName("test_user").setEmail("test_user@test.ru");
    private final NewUserDto newUserDto1 = new NewUserDto().setName("test_user1").setEmail("test_user@test.ru");
    private final UpdateUserDto updateUserDto = new UpdateUserDto().setName("update_name").setEmail("update_email");
    private final UpdateUserDto updateUserDto1 = new UpdateUserDto().setName("update_name").setEmail("test_user@test.ru");

    @Test
    @DisplayName("Поиск всех пользователей - пустой список")
    void testFindAllWithNoUsers() {
        List<UserDto> users = userService.findAll();
        Assertions.assertNotNull(users);
        Assertions.assertEquals(0, users.size());
    }

    @Test
    @DisplayName("Поиск всех пользователей - один пользователь")
    void testFindAllWith1User() {
        userService.save(newUserDto);
        List<UserDto> users = userService.findAll();
        Assertions.assertNotNull(users);
        Assertions.assertEquals(1, users.size());
    }

    @Test
    @DisplayName("Поиск пользователя по Id - правильный id")
    void testFindByIdWithProperId() {
        UserDto savedUser = userService.save(newUserDto);
        UserDto result = userService.findById(savedUser.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedUser.getId(), result.getId());
        Assertions.assertEquals(savedUser.getEmail(), result.getEmail());
        Assertions.assertEquals(savedUser.getName(), result.getName());
    }

    @Test
    @DisplayName("Поиск пользователя по Id - неправильный id")
    void testFindByIdWithWrongId() {
        userService.save(newUserDto);
        try {
            userService.findById(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск пользователя по Id - null id")
    void testFindByIdWithNullId() {
        userService.save(newUserDto);
        try {
            userService.findById(null);
        } catch (InvalidDataAccessApiUsageException ex) {
            Assertions.assertEquals("The given id must not be null", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Проверка на существующий email при сохранении")
    void testCheckEmailAndReturnErrorIfExists() {
        userService.save(newUserDto);
        try {
            userService.save(newUserDto1);
        } catch (ValidationException ex) {
            Assertions.assertEquals("Пользователь с email = test_user@test.ru уже существует", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Удаление пользователя с правильным id")
    void testDeleteWithProperId() {
        UserDto savedUser = userService.save(newUserDto);
        userService.deleteUser(savedUser.getId());
        try {
            userService.findById(savedUser.getId());
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = " + savedUser.getId() + " не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Удаление пользователя с неправильным id")
    void testDeleteWithWrongId() {
        userService.save(newUserDto);
        try {
            userService.deleteUser(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление пользователя с правильным id")
    void testUpdateWithProperId() {
        UserDto savedUser = userService.save(newUserDto);
        UserDto result = userService.update(savedUser.getId(), updateUserDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedUser.getId(), result.getId());
        Assertions.assertEquals(updateUserDto.getEmail(), result.getEmail());
        Assertions.assertEquals(updateUserDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Обновление пользователя с неправильным id")
    void testUpdateWithWrongId() {
        userService.save(newUserDto);
        try {
            userService.update(999L, updateUserDto);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Пользователь с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Обновление пользователя с существующим email")
    void testUpdateWithWrongEmail() {
        UserDto savedUser = userService.save(newUserDto);
        try {
            userService.update(savedUser.getId(), updateUserDto1);
        } catch (ValidationException ex) {
            Assertions.assertEquals("Пользователь с email = test_user@test.ru уже существует", ex.getMessage());
        }
    }
}
