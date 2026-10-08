package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewItemDto {

    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
