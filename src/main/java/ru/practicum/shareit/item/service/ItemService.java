package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemDto save(NewItemDto itemDto, long userId);

    ItemDto update(UpdateItemDto itemDto, long itemId, long userId);

    List<GetItemDto> findAllByUserId(long userId);

    GetItemDto findGetItemDtoByItemId(long itemId, long userId);

    List<ItemDto> search(String text);

    CommentDto saveComment(long userId, long itemId, NewCommentDto newCommentDto);
}
