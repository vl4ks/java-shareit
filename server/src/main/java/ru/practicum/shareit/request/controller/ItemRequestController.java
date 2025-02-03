package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                            @RequestBody ItemRequestSaveDto itemRequestSaveDto) {
        return itemRequestService.createItemRequest(userId, itemRequestSaveDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserItemRequest(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestService.getAllUserItemRequestsWithItems(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(
            @RequestHeader(HEADER_USER_ID) Long userId
    ) {
        return itemRequestService.getAllItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@PathVariable Long requestId) {
        log.info("Получение данных об одном конкретном запросе по requestId = {}", requestId);
        return itemRequestService.getItemRequest(requestId);
    }

}