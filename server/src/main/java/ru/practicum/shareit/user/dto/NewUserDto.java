package ru.practicum.shareit.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewUserDto {

    private String email;
    private String name;
}
