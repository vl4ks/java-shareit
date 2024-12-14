package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;


public interface ItemService {
    ItemDto getItem(Long itemId);

    ItemDto createItem(ItemDto itemDto, Long userId);

    CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto);

    List<ItemDto> searchItems(String text);

    ItemDto updateItem(ItemDto itemDto, Long userId);

    void deleteItem(User user, Long itemId);

    List<ItemDto> getItemsWithBookings(Long ownerId);
}
