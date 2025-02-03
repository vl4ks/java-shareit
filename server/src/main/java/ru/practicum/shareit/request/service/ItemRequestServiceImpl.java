package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static ru.practicum.shareit.request.service.ItemRequestMapper.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestSaveDto itemRequestSaveDto) {
        log.info("Создание запроса на предмет пользователем с id - {}, {}", userId, itemRequestSaveDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        ItemRequest itemRequest = toRequest(itemRequestSaveDto);
        itemRequest.setRequester(user);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Создан запрос на предмет пользователем с id - {}, {}", userId, savedItemRequest);
        return toRequestDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllUserItemRequestsWithItems(Long userId) {
        log.info("Получение всех запросов пользователя с id - {}", userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return ItemRequestMapper.toRequestDto(itemRequests);

    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        log.info("Получение всех запросов кроме пользователя с id - {}", userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequester_IdNotInOrderByCreatedDesc(List.of(userId));
        return ItemRequestMapper.toRequestDto(itemRequests);
    }

    @Override
    public ItemRequestDto getItemRequest(Long requestId) {
        log.info("Получение запроса с id - {}", requestId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new NotFoundException("Запрос с id " + requestId + " не найден."));
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        itemRequest.setItems(items);
        ItemRequestDto itemRequestDto = toRequestDto(itemRequest);
        log.info("Найден запрос {}", itemRequestDto);
        return itemRequestDto;
    }
}
