package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto save(@RequestHeader("X-Sharer-User-Id") long userId,
                               @RequestBody NewItemRequestDto itemRequestDto) {
        return itemRequestService.save(itemRequestDto, userId);
    }

    @GetMapping
    public List<GetItemRequestDto> findGetItemRequestDtoByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.findGetItemRequestDtoByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllItemRequestDto() {
        return itemRequestService.findAllItemRequestDto();
    }

    @GetMapping("/{requestId}")
    public GetItemRequestDto findGetItemRequestDtoById(@PathVariable long requestId) {
        return itemRequestService.findGetItemRequestDtoById(requestId);
    }
}
