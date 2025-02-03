package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private final User user = new User(1L, "User", "user@email.com");
    private final UserDto userDto = new UserDto(1L, "User", "user@email.com");
    private final User user2 = new User(2L, "User2", "user2@email.com");
    private final UserDto userDto2 = new UserDto(2L, "User2", "user@email.com");
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .requester(user)
            .description("description")
            .build();
    private final ItemRequestSaveDto itemRequestSaveDto = ItemRequestSaveDto.builder()
            .description("New item request")
            .build();

    @Test
    void testGetAllUserItemRequestsWithItems_ReturnItemRequests() {
        Long userId = user.getId();

        List<ItemRequest> itemRequests = List.of(itemRequest);
        Mockito.when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(itemRequests);

        List<ItemRequestDto> result = itemRequestService.getAllUserItemRequestsWithItems(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getRequester().getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findAllByRequesterIdOrderByCreatedDesc(user.getId());
    }

    @Test
    void testGetAllItemRequests_ReturnItemRequests() {
        itemRequest.setRequester(user2);
        List<ItemRequest> itemRequests = List.of(itemRequest);

        Mockito.when(itemRequestRepository.findAllByRequester_IdNotInOrderByCreatedDesc(List.of(user.getId())))
                .thenReturn(itemRequests);

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotEquals(user.getId(), result.get(0).getRequester().getId());
        Mockito.verify(itemRequestRepository).findAllByRequester_IdNotInOrderByCreatedDesc(List.of(user.getId()));
    }


    @Test
    void testGetItemRequest_ReturnItemRequestDto() {
        Item item = Item.builder()
                .id(1L)
                .request(itemRequest)
                .owner(user2)
                .build();

        Mockito.when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));

        Mockito.when(itemRepository.findAllByRequestId(itemRequest.getId()))
                .thenReturn(Collections.singletonList(item));

        ItemRequestDto result = itemRequestService.getItemRequest(itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(userDto.getId(), result.getRequester().getId());
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().get(0).getId());
        assertEquals(userDto2.getId(), result.getItems().get(0).getOwnerId());

        Mockito.verify(itemRequestRepository).findById(itemRequest.getId());
        Mockito.verify(itemRepository).findAllByRequestId(itemRequest.getId());
    }


    @Test
    void testCreateItemRequest_CreateAndReturnItemRequest() {
        ItemRequest savedItemRequest = ItemRequest.builder()
                .id(1L)
                .requester(user)
                .description(itemRequestSaveDto.getDescription())
                .build();

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.save(Mockito.any(ItemRequest.class)))
                .thenReturn(savedItemRequest);

        ItemRequestDto result = itemRequestService.createItemRequest(user.getId(), itemRequestSaveDto);

        assertNotNull(result);
        assertEquals(itemRequestSaveDto.getDescription(), result.getDescription());
        assertEquals(user.getId(), result.getRequester().getId());
        assertEquals(user.getName(), result.getRequester().getName());
        assertEquals(user.getEmail(), result.getRequester().getEmail());

        Mockito.verify(userRepository).findById(user.getId());
        Mockito.verify(itemRequestRepository).save(Mockito.any(ItemRequest.class));
    }

}
