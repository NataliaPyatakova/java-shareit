package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserMapper::mapToUserDto).toList();
    }

    @Override
    public UserDto findById(Long id) {
        return UserMapper.mapToUserDto(findUserById(id));
    }

    @Override
    public UserDto save(NewUserDto user) {
        log.info("save user {}", user);
        findByEmail(user.getEmail());
        return UserMapper.mapToUserDto(userRepository.save(UserMapper.mapToUser(user)));
    }

    @Override
    public UserDto update(Long id, UpdateUserDto user) {
        log.info("update user {}", user);
        User foundedUser = findUserById(id);
        findByEmail(user.getEmail());
        return UserMapper.mapToUserDto(userRepository.update(UserMapper.mapToUserForUpdate(foundedUser, user)));
    }

    @Override
    public void deleteUser(Long id) {
        log.info("delete user id {}", id);
        findById(id);
        userRepository.deleteUser(id);
    }

    private void findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new ValidationException("Пользователь с email = " + email + " уже существует");
        }
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }
}
