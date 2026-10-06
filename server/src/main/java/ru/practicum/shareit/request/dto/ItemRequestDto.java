package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ItemRequestDto {

    private Long id;
    private String description;
    private LocalDateTime created;
}
