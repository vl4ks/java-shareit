package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;


final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(String.valueOf(item.isAvailable()))
                .build();


    }

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(String.valueOf(item.isAvailable()))
                .lastBooking(lastBooking != null ? lastBooking.getStart() : null)
                .nextBooking(nextBooking != null ? nextBooking.getStart() : null)
                .comments(comments)
                .build();
    }

    public static Item toItem(ItemDto dto, Long id, ItemRequest request, User owner) {
        return Item.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .available(Boolean.parseBoolean(dto.getAvailable()))
                .request(request)
                .owner(owner)
                .build();
    }
}
