package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;


@RestController("itemController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsByOwnerId(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.debug("Получение вещей владельца с id = {}", userId);
        List<ItemDto> items = itemService.getItemsWithBookings(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                           @PathVariable Long itemId) {
        log.debug("Получение вещи по id владельца = {} и id вещи = {}", userId, itemId);
        ItemDto item = itemService.getItem(itemId);
        return ResponseEntity.ok(item);
    }


    @PostMapping
    public ResponseEntity<ItemDto> create(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.debug("Создание новой вещи: {}", itemDto);
        ItemDto createdItem = itemService.createItem(itemDto, userId);
        return ResponseEntity.ok(createdItem);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                                                 @PathVariable Long itemId,
                                                 @Valid @RequestBody CommentCreateDto commentDto) {
        log.debug("Добавление комментария для вещи с id = {} пользователем с id = {}", itemId, userId);
        CommentDto addedComment = itemService.addComment(itemId, userId, commentDto);
        return ResponseEntity.ok(addedComment);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        log.debug("Поиск вещи по тексту: {}", text);
        List<ItemDto> searchResults = itemService.searchItems(text);
        return ResponseEntity.ok(searchResults);
    }


    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @RequestBody ItemDto itemDto,
                                          @PathVariable Long itemId) {
        log.debug("Обновление вещи: {}", itemDto);
        itemDto.setId(itemId);
        ItemDto updatedItem = itemService.updateItem(itemDto, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@RequestHeader(HEADER_USER_ID) User user,
                                       @PathVariable Long itemId) {
        log.debug("Удаление вещи с id = {} ", itemId);
        itemService.deleteItem(user, itemId);
        return ResponseEntity.noContent().build();
    }
}
