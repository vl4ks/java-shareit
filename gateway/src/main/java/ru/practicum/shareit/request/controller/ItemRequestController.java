package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;

@Slf4j
@Controller
@RequestMapping("/requests")
@AllArgsConstructor
@Validated
public class ItemRequestController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                                    @RequestBody @Valid ItemRequestSaveDto itemRequestSaveDto) {
        return itemRequestClient.createItemRequest(userId, itemRequestSaveDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItemRequest(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestClient.getAllUserItemRequest(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @RequestHeader(HEADER_USER_ID) Long userId
    ) {
        return itemRequestClient.getAllItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@PathVariable @PositiveOrZero Long requestId) {
        log.info("Получение данных о запросе .");
        return itemRequestClient.getItemRequest(requestId);
    }
}
