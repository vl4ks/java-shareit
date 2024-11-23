package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findByUserId(Long userId);

    List<Item> getItemsByOwnerId(Long userId);

    Item getItem(Long itemId);

    Item createItem(Item item);

    Item updateItem(Item item);

    List<Item> searchItems(String text);

    void deleteItem(Long userId, Long itemId);
}
