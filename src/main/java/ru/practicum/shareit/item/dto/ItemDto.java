package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class ItemDto {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Boolean available;
}
