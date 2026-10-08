package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@SpringBootTest
public class UserMapperTest {

    private final User user = new User()
            .setId(1L)
            .setName("userName")
            .setEmail("email");
    private final UserDto userDto = new UserDto()
            .setId(1L)
            .setName("userName")
            .setEmail("email");
    private final NewUserDto newUserDto = new NewUserDto()
            .setName("userName")
            .setEmail("email");

    @Test
    public void testMapToUserDto() {
        UserDto result = UserMapper.mapToUserDto(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testMapToUser() {
        User result = UserMapper.mapToUser(userDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(userDto.getId(), result.getId());
        Assertions.assertEquals(userDto.getName(), result.getName());
        Assertions.assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    public void testMapToUserForCreate() {
        User result = UserMapper.mapToUser(newUserDto);
        Assertions.assertEquals(newUserDto.getName(), result.getName());
        Assertions.assertEquals(newUserDto.getEmail(), result.getEmail());
    }

    @Test
    public void testMapToUserForUpdateAllFields() {
        UpdateUserDto updateUserDto = new UpdateUserDto().setName("updatedUserName").setEmail("updatedEmail");
        User result = UserMapper.mapToUserForUpdate(user, updateUserDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(updateUserDto.getName(), result.getName());
        Assertions.assertEquals(updateUserDto.getEmail(), result.getEmail());
    }

    @Test
    public void testMapToUserForUpdateName() {
        UpdateUserDto updateUserDto = new UpdateUserDto().setName("updatedUserName");
        User result = UserMapper.mapToUserForUpdate(user, updateUserDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(updateUserDto.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testMapToUserForUpdateEmail() {
        UpdateUserDto updateUserDto = new UpdateUserDto().setEmail("updatedEmail");
        User result = UserMapper.mapToUserForUpdate(user, updateUserDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(updateUserDto.getEmail(), result.getEmail());
    }
}