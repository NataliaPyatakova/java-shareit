package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public ItemRequestDto save(NewItemRequestDto itemRequestDto, long userId) {
        log.info("save itemRequest {} for userId {}", itemRequestDto, userId);
        User user = UserMapper.mapToUser(userService.findById(userId));
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequestForCreate(itemRequestDto, user);
        itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.mapToItemRequestDto(itemRequest);
    }

    @Override
    public GetItemRequestDto findGetItemRequestDtoById(long id) {
        log.info("findGetItemRequestDtoById {}", id);
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + id + " не найден"));
        List<ItemDto> items = itemService.findAllByRequestId(id);
        return ItemRequestMapper.mapToGetItemRequestDto(itemRequest, items);
    }

    @Override
    public List<GetItemRequestDto> findGetItemRequestDtoByUserId(long userId) {
        log.info("findGetItemRequestDtoByUserId {}", userId);
        userService.findById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByUserId(userId);
        List<Long> itemRequestIds = itemRequests.stream().map(ItemRequest::getId).toList();
        List<Item> items = itemService.findAllByItemRequestIdIn(itemRequestIds);
        return itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.mapToGetItemRequestDto(itemRequest,
                        items.stream()
                                .filter(item -> item.getItemRequest().getId().equals(itemRequest.getId()))
                                .map(ItemMapper::mapToItemDto)
                                .toList()))
                .sorted(Comparator.comparing(GetItemRequestDto::getCreated))
                .toList();
    }

    @Override
    public List<ItemRequestDto> findAllItemRequestDto() {
        log.info("findAllItemRequestDto");
        return itemRequestRepository.findAll().stream()
                .map(ItemRequestMapper::mapToItemRequestDto)
                .sorted(Comparator.comparing(ItemRequestDto::getCreated))
                .toList();
    }
}
