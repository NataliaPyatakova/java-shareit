package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.GetItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto mapToItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setUserId(item.getUser().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }

    public static GetItemDto mapToGetItemDto(Item item, List<Comment> listComments,
                                             LocalDateTime lastBooking, LocalDateTime nextBooking) {
        GetItemDto dto = new GetItemDto();
        dto.setId(item.getId());
        dto.setUserId(item.getUser().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setComments(listComments.stream().map(CommentMapper::commentToCommentDto).toList());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        return dto;
    }

    public static Item mapToItemForCreate(NewItemDto itemDto, User user) {
        Item item = new Item();
        item.setUser(user);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static Item mapToItemForUpdate(UpdateItemDto itemDto, Item foundedItem) {
        Item item = new Item();
        item.setId(foundedItem.getId());
        item.setUser(foundedItem.getUser());
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        } else {
            item.setName(foundedItem.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        } else {
            item.setDescription(foundedItem.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        } else {
            item.setAvailable(foundedItem.getAvailable());
        }
        return item;
    }
}