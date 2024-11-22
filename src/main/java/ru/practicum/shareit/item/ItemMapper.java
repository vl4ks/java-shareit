package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

@Component("itemMapper")
@RequiredArgsConstructor
public class ItemMapper {
    public ItemDto toItemDto(Item item, Long ownerId) {
        return toItemDto(item).toBuilder().ownerId(ownerId).build();

    }

    public ItemDto toItemDto(Item item) {
        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(String.valueOf(item.isAvailable()))
                .ownerId(item.getOwnerId())
                .build();


    }

    public Item toItem(ItemDto dto, Long id, ItemRequest request) {
        return new Item().toBuilder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .available(Boolean.parseBoolean(dto.getAvailable()))
                .request(request)
                .build();

    }

    public Item toItem(ItemDto dto, Long id, ItemRequest request, Long ownerId) {
        return toItem(dto, id, request).toBuilder().ownerId(ownerId).build();
    }
}
