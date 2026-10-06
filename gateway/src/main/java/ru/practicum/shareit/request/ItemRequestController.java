package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> save(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @Validated @RequestBody NewItemRequestDto itemRequestDto) {
        log.info("Gateway requests save");
        return itemRequestClient.save(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findGetItemRequestDtoByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Gateway requests findGetItemRequestDtoByUserId");
        return itemRequestClient.findGetItemRequestDtoByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllItemRequestDto() {
        log.info("Gateway requests findAllItemRequestDto");
        return itemRequestClient.findAllItemRequestDto();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findGetItemRequestDtoById(@PathVariable @NotNull long requestId) {
        log.info("Gateway requests findGetItemRequestDtoById");
        return itemRequestClient.findGetItemRequestDtoById(requestId);
    }
}