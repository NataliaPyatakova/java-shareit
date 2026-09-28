package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByUserId(long userId);

    @Query("select it from Item as it where (lower(it.name) like :text or lower(it.description) like :text) and it.available = true")
    List<Item> search(@Param("text") String text);
}
