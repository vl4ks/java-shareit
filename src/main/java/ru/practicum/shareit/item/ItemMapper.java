package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(String.valueOf(item.isAvailable()))
                .build();


    }

    public static Item toItem(ItemDto dto, Long id, ItemRequest request, Long ownerId) {
        return Item.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .available(Boolean.parseBoolean(dto.getAvailable()))
                .request(request)
                .ownerId(ownerId)
                .build();
    }
}
