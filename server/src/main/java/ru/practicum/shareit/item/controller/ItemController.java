package ru.practicum.shareit.item.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@RestController("itemController")
@Slf4j
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;


    @GetMapping
    List<ItemDto> getItemsByOwnerId(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemService.getItemsWithBookings(userId);
    }


    @GetMapping("/{itemId}")
    ItemDto getItem(@PathVariable Long itemId) {
        return itemService.getItem(itemId);
    }

    @PostMapping
    ItemDto create(@RequestHeader(HEADER_USER_ID) Long userId,
                    @RequestBody ItemSaveDto itemSaveDto) {
        log.info("Создание вещи - {} пользователем с id = {}", itemSaveDto, userId);
        return itemService.createItem(userId, itemSaveDto);
    }


    @PostMapping("/{itemId}/comment")
    CommentDto addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody CommentSaveDto commentSaveDto) {
        log.info("Добавление комментария пользователем с id = {} для вещи с id = {}", userId, itemId);
        return itemService.addComment(userId, itemId, commentSaveDto);
    }


    @GetMapping("/search")
    List<ItemDto> search(@RequestParam String text) {
        return itemService.searchItems(text);
    }


    @PatchMapping("/{itemId}")
    ItemDto updateItem(@RequestHeader(HEADER_USER_ID) Long userId,
                       @PathVariable Long itemId,
                       @RequestBody ItemSaveDto itemSaveDto) {
        return itemService.updateItem(itemSaveDto, userId, itemId);
    }


    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(HEADER_USER_ID) User user,
                                       @PathVariable Long itemId) {
        log.debug("Удаление вещи с id = {} ", itemId);
        itemService.deleteItem(user, itemId);
    }

}
