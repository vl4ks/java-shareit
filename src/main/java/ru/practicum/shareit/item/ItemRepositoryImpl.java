package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("itemRepositoryImpl")
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private long idGenerator = 0;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public List<Item> findByUserId(Long userId) {
        log.info("Получение пользователя по id=: {}", userId);
        return itemStorage.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        log.info("Получение вещей владельца с id=: {}", userId);
        UserDto owner = userService.getUserById(userId);

        return findByUserId(userId).stream()
                .map(item -> itemMapper.toItemDto(item, owner.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItem(Long itemId) {
        log.info("Получение вещи по id=: {}",  itemId);
        if (!itemStorage.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена.");
        }
        return itemStorage.get(itemId);
    }


    @Override
    public Item createItem(ItemDto itemDto) {
        log.info("Создание новой вещи: {}", itemDto);
        idGenerator++;
        Item newItem = itemMapper.toItem(itemDto, idGenerator, new ItemRequest(), itemDto.getOwnerId());
        if (itemStorage.containsKey(idGenerator)) {
            idGenerator--;
            throw new ConflictException("Предмет с id " + idGenerator + " уже существует.");
        }
        itemStorage.put(idGenerator, newItem);
        return itemStorage.get(idGenerator);
    }

    @Override
    public List<Item> searchItems(String text) {
        log.info("Поиск вещи по тексту: {}", text);
        String lowerCaseText = text.toLowerCase();
        return itemStorage.values().stream()
                .filter(item -> item.isAvailable()
                        && (item.getName().toLowerCase().contains(lowerCaseText)
                        || item.getDescription().toLowerCase().contains(lowerCaseText)))
                .toList();
    }


    @Override
    public Item updateItem(ItemDto itemDto) {
        log.info("Обновление вещи с id: {}", itemDto.getId());

        Item existingItem = validateItemOwnership(itemDto.getOwnerId(), itemDto.getId());

        updateFields(existingItem, itemDto);

        itemStorage.put(existingItem.getId(), existingItem);
        log.info("Вещь с id: {} успешно обновлена", existingItem.getId());

        return existingItem;
    }

    private void updateFields(Item existingItem, ItemDto itemDto) {
        if (itemDto.getName() != null && !existingItem.getName().equals(itemDto.getName())) {
            log.debug("Обновление имени вещи: '{}' -> '{}'", existingItem.getName(), itemDto.getName());
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !existingItem.getDescription().equals(itemDto.getDescription())) {
            log.debug("Обновление описания вещи: '{}' -> '{}'", existingItem.getDescription(), itemDto.getDescription());
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            boolean newAvailability = Boolean.parseBoolean(itemDto.getAvailable());
            if (existingItem.isAvailable() != newAvailability) {
                log.debug("Обновление доступности вещи: '{}' -> '{}'", existingItem.isAvailable(), newAvailability);
                existingItem.setAvailable(newAvailability);
            }
        }
    }

    private Item validateItemOwnership(Long ownerId, Long itemId) {
        if (!itemStorage.containsKey(itemId)) {
            log.warn("Вещь с id: {} не найдена", itemId);
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }

        Item item = itemStorage.get(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            log.warn("Пользователь с id: {} попытался получить доступ к вещи с id: {}, но не является её владельцем",
                    ownerId, itemId);
            throw new ForbiddenException("Доступ запрещён. Вы не являетесь владельцем вещи с id " + itemId);
        }
        return item;
    }



    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Удаление вещи с id: {}", itemId);

        validateItemOwnership(userId, itemId);

        itemStorage.remove(itemId);
        log.info("Вещь с id: {} успешно удалена", itemId);
    }
}
