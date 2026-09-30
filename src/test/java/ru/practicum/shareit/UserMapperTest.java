package ru.practicum.shareit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@SpringBootTest
public class UserMapperTest {

    private final User user = new User().setId(1L).setName("userName").setEmail("email");
    private final UserDto userDto = new UserDto().setId(1L).setName("userName").setEmail("email");
    private final NewUserDto newUserDto = new NewUserDto().setName("userName").setEmail("email");
    private final User updatedUser = new User().setId(2L).setName("updatedUserName").setEmail("updatedEmail");
    private final UpdateUserDto updateUserDto = new UpdateUserDto().setName("updatedUserName").setEmail("updatedEmail");

    @Test
    public void testMapToUserDto() {
        UserDto result = UserMapper.mapToUserDto(user);
        Assertions.assertThat(result.getId()).isEqualTo(user.getId());
        Assertions.assertThat(result.getName()).isEqualTo(user.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void testMapToUser() {
        User result = UserMapper.mapToUser(userDto);
        Assertions.assertThat(result.getId()).isEqualTo(userDto.getId());
        Assertions.assertThat(result.getName()).isEqualTo(userDto.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    public void testMapToUserForCreate() {
        User result = UserMapper.mapToUser(newUserDto);
        Assertions.assertThat(result.getName()).isEqualTo(newUserDto.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(newUserDto.getEmail());
    }

    @Test
    public void testMapToUserForUpdateAllFields() {
        User result = UserMapper.mapToUserForUpdate(updatedUser, updateUserDto);
        Assertions.assertThat(result.getId()).isEqualTo(updatedUser.getId());
        Assertions.assertThat(result.getName()).isEqualTo(updateUserDto.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(updateUserDto.getEmail());
    }

    @Test
    public void testMapToUserForUpdateName() {
        updateUserDto.setEmail(null);
        updateUserDto.setName("updatedUserName");
        User result = UserMapper.mapToUserForUpdate(updatedUser, updateUserDto);
        Assertions.assertThat(result.getId()).isEqualTo(updatedUser.getId());
        Assertions.assertThat(result.getName()).isEqualTo(updateUserDto.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(updatedUser.getEmail());
    }

    @Test
    public void testMapToUserForUpdateEmail() {
        updateUserDto.setEmail("updatedEmail");
        updateUserDto.setName(null);
        User result = UserMapper.mapToUserForUpdate(updatedUser, updateUserDto);
        Assertions.assertThat(result.getId()).isEqualTo(updatedUser.getId());
        Assertions.assertThat(result.getName()).isEqualTo(updatedUser.getName());
        Assertions.assertThat(result.getEmail()).isEqualTo(updateUserDto.getEmail());
    }
}
