package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto save(NewItemDto itemDto, long userId) {
        log.info("save item {} for userId {}", itemDto, userId);
        User user = UserMapper.mapToUser(userService.findById(userId));
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id = " + itemDto.getRequestId() + " не найден"));
        }
        return ItemMapper.mapToItemDto(itemRepository.save(ItemMapper.mapToItemForCreate(itemDto, user, request)));
    }

    @Override
    @Transactional
    public ItemDto update(UpdateItemDto itemDto, long itemId, long userId) {
        log.info("update item {} with itemId {} for userId {}", itemDto, itemId, userId);
        userService.findById(userId);
        Item foundedItem = findItemById(itemId);
        log.info("foundedItem for update {}", foundedItem);
        return ItemMapper.mapToItemDto(itemRepository.save(ItemMapper.mapToItemForUpdate(itemDto, foundedItem)));
    }

    @Override
    public List<GetItemDto> findAllByUserId(long userId) {
        log.info("findAllByUserId {}", userId);
        userService.findById(userId);
        List<Item> items = itemRepository.findAllByUserId(userId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<BookingDto> bookingList = bookingService.findAllByItemIdInAndState(itemIds, BookingProcessState.APPROVED);
        Map<Long, LocalDateTime> itemLastBooking = new HashMap<>();
        Map<Long, LocalDateTime> itemLNextBooking = new HashMap<>();
        items.forEach(item ->
                itemLastBooking.put(item.getId(), bookingList.stream()
                        .filter(bookingDto -> bookingDto.getItem().getId().equals(item.getId()))
                        .map(BookingDto::getEnd)
                        .filter(this::checkEndDate)
                        .max(LocalDateTime::compareTo)
                        .orElse(null)));
        items.forEach(item ->
                itemLNextBooking.put(item.getId(), bookingList.stream()
                        .filter(bookingDto -> bookingDto.getItem().getId().equals(item.getId()))
                        .map(BookingDto::getStart)
                        .filter(this::checkStartDate)
                        .min(LocalDateTime::compareTo)
                        .orElse(null)));
        List<Comment> listComments = commentRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<Comment>> mapComments = new HashMap<>();
        if (listComments != null) {
            listComments.forEach(comment -> {
                List<Comment> list = mapComments.computeIfAbsent(comment.getItem().getId(), k -> new ArrayList<>());
                list.add(comment);
            });
        }
        return items.stream()
                .map(item -> ItemMapper.mapToGetItemDto(item,
                        mapComments.get(item.getId())!= null ? mapComments.get(item.getId()) : new ArrayList<>(),
                        itemLastBooking.get(item.getId()),
                        itemLNextBooking.get(item.getId())))
                .toList();
    }

    @Override
    public GetItemDto findGetItemDtoByItemId(long itemId, long userId) {
        log.info("findGetItemDtoByItemId {}", itemId);
        Item item = findItemById(itemId);
        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;
        if (item.getUser().getId().equals(userId)) {  //читаем даты только для владельца вещи
            List<BookingDto> bookingList = bookingService.findByItemIdAndState(itemId, BookingProcessState.APPROVED);
            lastBooking = bookingList.stream()
                    .map(BookingDto::getEnd)
                    .filter(this::checkEndDate)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            nextBooking = bookingList.stream()
                    .map(BookingDto::getStart)
                    .filter(this::checkStartDate)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
        }
        List<Long> itemIds = new ArrayList<>();
        itemIds.add(item.getId());
        List<Comment> listComments = commentRepository.findAllByItemIdIn(itemIds);
        return ItemMapper.mapToGetItemDto(item, listComments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("search by text {}", text);
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return itemRepository.search(text.toLowerCase()).stream().map(ItemMapper::mapToItemDto).toList();
    }

    @Override
    @Transactional
    public CommentDto saveComment(long bookerId, long itemId, NewCommentDto newCommentDto) {
        log.info("save comment {} from bookerId {} for itemId {}", newCommentDto, bookerId, itemId);
        User user = UserMapper.mapToUser(userService.findById(bookerId));
        Item item = findItemById(itemId);
        bookingService.findPastBookingByBookerIdAndItemId(bookerId, itemId);
        Comment comment = commentRepository.save(CommentMapper.commentDtoToCommentForCreate(user, item, newCommentDto));
        return CommentMapper.commentToCommentDto(comment);
    }

    @Override
    public List<ItemDto> findAllByRequestId(long requestId) {
        log.info("findAllByRequestId {}", requestId);
        itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));
        return itemRepository.findAllByItemRequestId(requestId).stream().map(ItemMapper::mapToItemDto).toList();
    }

    @Override
    public List<Item> findAllByItemRequestIdIn(List<Long> itemRequestIds) {
        log.info("findAllByItemRequestIdIn");
        return itemRepository.findAllByItemRequestIdIn(itemRequestIds);
    }

    private Item findItemById(long itemId) {
        log.info("findItemById {}", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    private boolean checkStartDate(LocalDateTime startDate) {
        log.info("checkStartDate {}", startDate);
        return startDate.isAfter(LocalDateTime.now());
    }

    private boolean checkEndDate(LocalDateTime endDate) {
        log.info("checkEndDate {}", endDate);
        return endDate.isBefore(LocalDateTime.now());
    }
}