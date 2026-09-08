package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepositoryInMemoryImpl implements ItemRepository {

    Map<Long, List<Item>> itemsByUser = new HashMap<>();
    List<Item> allItems = new ArrayList<>();

    @Override
    public Item save(Item item) {
        item.setId(getId());
        allItems.add(item);
        itemsByUser.computeIfAbsent(item.getUserId(), k -> new ArrayList<>()).add(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        allItems.removeIf(item1 -> item1.getId().equals(item.getId()));
        allItems.add(item);
        itemsByUser.get(item.getUserId()).removeIf(item2 -> item2.getId().equals(item.getId()));
        itemsByUser.get(item.getUserId()).add(item);
        return item;
    }

    @Override
    public List<Item> findAllByUserId(long userId) {
        return Collections.unmodifiableList(itemsByUser.get(userId));
    }

    @Override
    public Optional<Item> findByItemId(long itemId) {
        return allItems.stream().filter(item -> item.getId().equals(itemId)).findFirst();
    }

    @Override
    public List<Item> search(String text) {
        return allItems.stream()
                .filter(item -> (item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text))
                        && item.getAvailable())
                .toList();
    }

    private long getId() {
        long lastId = allItems.stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }
}
