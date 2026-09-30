package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto save(NewItemRequestDto itemRequestDto, long userId);

    GetItemRequestDto findGetItemRequestDtoById(long id);

    List<GetItemRequestDto> findGetItemRequestDtoByUserId(long userId);

    List<ItemRequestDto> findAllItemRequestDto();

}
