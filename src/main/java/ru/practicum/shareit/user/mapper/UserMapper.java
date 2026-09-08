package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        return dto;
    }

    public static User mapToUser(NewUserDto userDto) {
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        return user;
    }

    public static User mapToUserForUpdate(User foundedUser, UpdateUserDto userDto) {
        User user = new User();
        user.setId(foundedUser.getId());
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        } else {
            user.setEmail(foundedUser.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        } else {
            user.setName(foundedUser.getName());
        }
        return user;
    }
}
