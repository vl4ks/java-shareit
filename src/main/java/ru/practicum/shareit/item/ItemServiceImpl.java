package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        return repository.getItemsByOwnerId(userId);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return itemMapper.toItemDto(repository.getItem(itemId));
    }


    @Override
    public ItemDto createItem(ItemDto itemDto) {
        userService.getUserById(itemDto.getOwnerId());
        return itemMapper.toItemDto(repository.createItem(itemDto), userService.getUserById(itemDto.getOwnerId()).getId());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return repository.searchItems(text).stream()
                .map(item -> itemMapper.toItemDto(item, userService.getUserById(item.getOwnerId()).getId()))
                .collect(Collectors.toList());
    }


    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        return itemMapper.toItemDto(repository.updateItem(itemDto));
    }


    @Override
    public void deleteItem(Long userId, Long itemId) {
        repository.deleteItem(userId, itemId);
    }

}
