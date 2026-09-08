package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;

public interface ItemService {

    ItemDto save(NewItemDto itemDto, long userId);

    ItemDto update(UpdateItemDto itemDto, long itemId, long userId);

    List<ItemDto> findAllByUserId(long userId);

    ItemDto findByItemId(long itemId);

    List<ItemDto> search(String text);
}
