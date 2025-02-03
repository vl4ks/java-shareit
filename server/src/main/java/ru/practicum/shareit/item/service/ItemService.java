package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemService {
    ItemDto getItem(Long itemId);

    ItemDto createItem(Long userId, ItemSaveDto itemSaveDto);

    CommentDto addComment(Long userId, Long itemId, CommentSaveDto commentDto);

    List<ItemDto> searchItems(String text);

    ItemDto updateItem(ItemSaveDto itemSaveDto, Long userId, Long itemId);

    void deleteItem(User user, Long itemId);

    List<ItemDto> getItemsWithBookings(Long ownerId);
}
