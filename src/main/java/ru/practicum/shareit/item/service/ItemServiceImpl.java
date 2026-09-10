package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto save(NewItemDto itemDto, long userId) {
        log.info("save item {} for userId {}", itemDto, userId);
        userService.findById(userId);
        return ItemMapper.mapToItemDto(itemRepository.save(ItemMapper.mapToItemForCreate(itemDto, userId)));
    }

    @Override
    public ItemDto update(UpdateItemDto itemDto, long itemId, long userId) {
        log.info("update item {} with itemId {} for userId {}", itemDto, itemId, userId);
        userService.findById(userId);
        Item foundedItem = findItemById(itemId);
        log.info("foundedItem for update {}", foundedItem);
        return ItemMapper.mapToItemDto(itemRepository.update(ItemMapper.mapToItemForUpdate(itemDto, foundedItem)));
    }

    @Override
    public List<ItemDto> findAllByUserId(long userId) {
        log.info("find all for userId {}", userId);
        userService.findById(userId);
        return itemRepository.findAllByUserId(userId).stream().map(ItemMapper::mapToItemDto).toList();
    }

    @Override
    public ItemDto findByItemId(long itemId) {
        log.info("find item {}", itemId);
        return ItemMapper.mapToItemDto(findItemById(itemId));
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("search by text {}", text);
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return itemRepository.search(text.toLowerCase()).stream().map(ItemMapper::mapToItemDto).toList();
    }

    private Item findItemById(long itemId) {
        return itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найден"));
    }
}