package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController("itemController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Получение вещей владельца с id=: {}", userId);
        return itemService.getItemsByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                           @PathVariable("itemId") Long itemId) {
        log.debug("Получение вещи по id владельца = {} и id вещи =: {}", userId, itemId);
        return itemService.getItem(itemId);
    }


    @PostMapping
    public ResponseEntity<ItemDto> create(
            @RequestHeader(value = "X-Sharer-User-Id") String userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.debug("Создание новой вещи: {}", itemDto);
        itemDto.setOwnerId(Long.parseLong(userId));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Sharer-User-Id", String.valueOf(itemDto.getOwnerId()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(itemService.createItem(itemDto));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.debug("Поиск вещи по тексту: {}", text);
        return itemService.searchItems(text);
    }


    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") String userId,
                                          @RequestBody ItemDto itemDto,
                                          @PathVariable Long itemId) {
        log.debug("Обновление вещи: {}", itemDto);
        itemDto.setOwnerId(Long.parseLong(userId));
        itemDto.setId(itemId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Sharer-User-Id", String.valueOf(itemDto.getOwnerId()));
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(itemService.updateItem(itemDto));

    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Later-User-Id") String userId,
                                       @PathVariable(name = "itemId") Long itemId) {
        log.debug("Удаление вещи с id: {} ", itemId);
        Long ownerId = Long.parseLong(userId);

        itemService.deleteItem(ownerId, itemId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Sharer-User-Id", userId);

        return ResponseEntity.noContent()
                .headers(responseHeaders).build();
    }
}
