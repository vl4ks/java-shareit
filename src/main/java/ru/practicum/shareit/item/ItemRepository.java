package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findByUserId(Long userId);

    List<ItemDto> getItemsByOwnerId(Long userId);

    Item getItem(Long itemId);

    Item createItem(ItemDto itemDto);

    Item updateItem(ItemDto itemDto);

    List<Item> searchItems(String text);

    void deleteItem(Long userId, Long itemId);
}
