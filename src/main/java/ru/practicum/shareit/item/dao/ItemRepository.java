package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item save(Item item);

    Item update(Item item);

    List<Item> findAllByUserId(long userId);

    Optional<Item> findByItemId(long itemId);

    List<Item> search(String text);
}
