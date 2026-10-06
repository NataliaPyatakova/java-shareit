package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class UpdateItemDto {

    private String name;
    private String description;
    private Boolean available;
}
