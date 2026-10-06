package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

@RestController
@RequestMapping("/items")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> save(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @Validated @RequestBody NewItemDto itemDto) {
        log.info("Gateway Items save");
        return itemClient.save(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @PathVariable long itemId,
                          @Validated @RequestBody UpdateItemDto itemDto) {
        log.info("Gateway Items update");
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Gateway Items findAllByUserId");
        return itemClient.findAllByUserId(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findByItemId(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @PathVariable @NotNull long itemId) {
        log.info("Gateway Items findByItemId");
        return itemClient.findByItemId(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Gateway Items search");
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> saveComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable @NotNull long itemId,
                                  @Validated @RequestBody NewCommentDto newCommentDto) {
        log.info("Gateway Items saveComment");
        return itemClient.saveComment(userId, itemId, newCommentDto);
    }
}
