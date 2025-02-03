package ru.practicum.shareit.item.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public final class ItemMapper {
    private ItemMapper() {
    }
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }


    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking != null ? lastBooking.getStart() : null)
                .nextBooking(nextBooking != null ? nextBooking.getStart() : null)
                .comments(comments)
                .build();
    }

    public static Item toItem(ItemSaveDto dto, Long id, ItemRequest request, User owner) {
        return Item.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .request(request)
                .owner(owner)
                .build();
    }

}
