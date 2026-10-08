package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class GetItemRequestDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}
