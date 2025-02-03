package ru.practicum.shareit.request.service;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.dto.ItemResponseToRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.service.UserMapper.toUserDto;

final class ItemRequestMapper {
    public static ItemRequestDto toRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                toUserDto(request.getRequester()),
                request.getCreated(),
                request.getItems() != null ?
                        toItemResponseDto(request.getItems()) :
                        new ArrayList<>()
        );
    }

    public static ItemRequest toRequest(ItemRequestSaveDto requestSaveDto) {
        return new ItemRequest(
                null,
                requestSaveDto.getDescription(),
                null,
                null
        );
    }

    public static List<ItemRequestDto> toRequestDto(List<ItemRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }
        return requests.stream()
                .map(ItemRequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public static List<ItemResponseToRequestDto> toItemResponseDto(List<Item> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> new ItemResponseToRequestDto(
                        item.getId(),
                        item.getName() != null ? item.getName() : "Unknown",
                        item.getOwner() != null ? item.getOwner().getId() : null
                ))
                .collect(Collectors.toList());
    }

}
