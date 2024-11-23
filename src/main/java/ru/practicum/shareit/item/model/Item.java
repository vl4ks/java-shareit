package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private Long ownerId;
    private ItemRequest request;
}
