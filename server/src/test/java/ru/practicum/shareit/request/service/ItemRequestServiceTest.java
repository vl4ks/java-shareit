package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(userRepository, itemRepository, itemRequestRepository);

        User user = new User(null, "User1", "user1@email.com");
        userRepository.save(user);
    }

    @Test
    void testCreateItemRequestIntegration() {
        User user = userRepository.findAll().getFirst();
        Long userId = user.getId();

        ItemRequestSaveDto requestDto = new ItemRequestSaveDto("Хочу взять в аренду ноутбук");

        ItemRequestDto createdRequest = itemRequestService.createItemRequest(userId, requestDto);

        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals("Хочу взять в аренду ноутбук", createdRequest.getDescription());

        ItemRequest savedRequest = itemRequestRepository.findById(createdRequest.getId()).orElse(null);
        assertNotNull(savedRequest);
        assertEquals("Хочу взять в аренду ноутбук", savedRequest.getDescription());
        assertEquals(userId, savedRequest.getRequester().getId());
    }

    @Test
    void testCreateItemRequest_ThrowNotFoundException() {
        Long nonExistentUserId = 999L;
        ItemRequestSaveDto requestDto = new ItemRequestSaveDto("Хочу взять в аренду велосипед");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(nonExistentUserId, requestDto));

        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }
}
