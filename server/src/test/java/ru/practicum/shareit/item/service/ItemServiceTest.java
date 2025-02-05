package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private ItemService itemService;

    private User user;

    private ItemRequest itemRequest;
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;


    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository,
                itemRequestRepository);
        user = userRepository.save(new User(null, "User1", "user1@email.com"));

        itemRequest = itemRequestRepository.save(new ItemRequest(null, "Нужен ноутбук", user,
                LocalDateTime.now()));
    }

    @Test
    void testCreateItemIntegration() {
        ItemSaveDto itemSaveDto = new ItemSaveDto("Ноутбук", "Игровой ноутбук", true, itemRequest.getId());

        ItemDto createdItem = itemService.createItem(user.getId(), itemSaveDto);

        assertNotNull(createdItem);
        assertNotNull(createdItem.getId());
        assertEquals("Ноутбук", createdItem.getName());
        assertEquals("Игровой ноутбук", createdItem.getDescription());
        assertEquals(true, createdItem.getAvailable());

        Item savedItem = itemRepository.findById(createdItem.getId()).orElse(null);
        assertNotNull(savedItem);
        assertEquals("Ноутбук", savedItem.getName());
        assertEquals(user.getId(), savedItem.getOwner().getId());
        assertEquals(itemRequest.getId(), savedItem.getRequest().getId());
    }

    @Test
    void testCreateItem_ThrowNotFoundException_IfUserNotFound() {
        ItemSaveDto itemSaveDto = new ItemSaveDto("Книга", "Изучаю Java с ЯП", true, null);

        Long nonExistentUserId = 999L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createItem(nonExistentUserId, itemSaveDto)
        );

        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void testCreateItem_ThrowNotFoundException_IfRequestNotFound() {
        ItemSaveDto itemSaveDto = new ItemSaveDto("Телефон", "Смартфон", true, 999L);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createItem(user.getId(), itemSaveDto)
        );

        assertEquals("Запрос с id = 999 не найден", exception.getMessage());
    }
}
