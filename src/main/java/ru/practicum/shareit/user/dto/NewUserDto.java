package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewUserDto {

    @Email
    @NotNull
    private String email;
    private String name;
}
