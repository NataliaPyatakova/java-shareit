package ru.practicum.shareit.user;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;


@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Gateway Users findAll");
        return userClient.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable("id") @NotNull Long id) {
        log.info("Gateway Users findById");
        return userClient.findById(id);
    }

    @PostMapping
    public ResponseEntity<Object> save(@Validated @RequestBody NewUserDto user) {
        log.info("Gateway Users save");
        return userClient.save(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") @NotNull Long id,
                                         @Validated @RequestBody UpdateUserDto user) {
        log.info("Gateway Users update");
        return userClient.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Gateway Users deleteUser");
        userClient.deleteUser(id);
    }
}
