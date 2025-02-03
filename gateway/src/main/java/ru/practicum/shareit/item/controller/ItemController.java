package ru.practicum.shareit.item.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @RequestBody @Validated(ValidationGroups.Create.class) ItemSaveDto itemSaveDto) {
        log.info("Создание вещи с id = {} пользователем с id = {}", itemSaveDto, userId);
        return itemClient.createItem(userId, itemSaveDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Validated(ValidationGroups.Create.class)
                                             CommentSaveDto commentSaveDto) {
        log.info("Добавление комментария пользователем с id = {} для вещи с id = {}", userId, itemId);
        return itemClient.addComment(userId, itemId, commentSaveDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody
                                             ItemSaveDto itemSaveDto) {
        return itemClient.updateItem(userId, itemId, itemSaveDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable Long itemId) {
        return itemClient.getItem(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemClient.getAllOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam @NotBlank String text) {
        return text.isBlank() ? ResponseEntity.ok(List.of()) : itemClient.searchItems(text);
    }

}
