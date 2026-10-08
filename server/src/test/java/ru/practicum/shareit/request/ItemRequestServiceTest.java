package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.GetItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.util.List;


@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;
    private final EntityManager em;

    private final User owner = new User()
            .setName("test_owner")
            .setEmail("test_owner@test.ru");
    private final User requester = new User()
            .setName("test_requester")
            .setEmail("test_requester@test.ru");
    private final Item item = new Item()
            .setName("test_item")
            .setDescription("test description")
            .setAvailable(true);
    private final NewItemRequestDto newItemRequestDto = new NewItemRequestDto()
            .setDescription("test");

    @Test
    @DisplayName("Поиск всех запросов - пустой список")
    public void testFindAllItemRequestDtoWithNoRequests() {
        List<ItemRequestDto> listItemRequestDto = itemRequestService.findAllItemRequestDto();
        Assertions.assertNotNull(listItemRequestDto);
        Assertions.assertEquals(0, listItemRequestDto.size());
    }

    @Test
    @DisplayName("Поиск всех запросов - 1 запрос")
    public void testFindAllItemRequestDtoWith1Request() {
        em.persist(requester);
        ItemRequestDto request = itemRequestService.save(newItemRequestDto, requester.getId());
        List<ItemRequestDto> result = itemRequestService.findAllItemRequestDto();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(request.getId(), result.getFirst().getId());
        Assertions.assertEquals(request.getDescription(), result.getFirst().getDescription());
    }

    @Test
    @DisplayName("Поиск запроса по id - правильный id")
    public void testFindGetItemRequestDtoByProperId() {
        em.persist(requester);
        ItemRequestDto savedRequest = itemRequestService.save(newItemRequestDto, requester.getId());
        em.persist(owner);
        item.setUser(owner);
        item.setItemRequest(ItemRequestMapper.mapToItemRequest(savedRequest, requester));
        em.persist(item);
        GetItemRequestDto result = itemRequestService.findGetItemRequestDtoById(savedRequest.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedRequest.getId(), result.getId());
        Assertions.assertEquals(savedRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(item.getId(), result.getItems().getFirst().getId());
        Assertions.assertEquals(item.getName(), result.getItems().getFirst().getName());
    }

    @Test
    @DisplayName("Поиск запроса по id - неправильный id")
    public void testFindGetItemRequestDtoByWrongId() {
        em.persist(requester);
        itemRequestService.save(newItemRequestDto, requester.getId());
        try {
            itemRequestService.findGetItemRequestDtoById(999L);
        } catch (NotFoundException ex) {
            Assertions.assertEquals("Запрос с id = 999 не найден", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск запросов по пользователю - правильный id")
    public void testFindGetItemRequestDtoByProperUserId() {
        em.persist(requester);
        itemRequestService.save(newItemRequestDto, requester.getId());
        List<GetItemRequestDto> result = itemRequestService.findGetItemRequestDtoByUserId(requester.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Поиск запросов по пользователю - правильный id - нет запросов")
    public void testFindGetItemRequestDtoByProperUserIdWithNoRequests() {
        em.persist(requester);
        List<GetItemRequestDto> result = itemRequestService.findGetItemRequestDtoByUserId(requester.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }
}