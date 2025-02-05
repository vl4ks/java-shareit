package ru.practicum.shareit.request.dto;


import lombok.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;
    private String description;

    private UserDto requester;

    private LocalDateTime created;

    private List<ItemResponseToRequestDto> items;
}
