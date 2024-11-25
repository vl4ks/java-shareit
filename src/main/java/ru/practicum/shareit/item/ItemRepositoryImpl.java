package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private long idGenerator = 0;

    @Override
    public List<Item> findByUserId(Long userId) {
        log.info("Получение пользователя по id = {}", userId);
        return itemStorage.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> getItemsByOwnerId(Long userId) {
        log.info("Получение вещей владельца с id = {}", userId);
        return findByUserId(userId);
    }

    @Override
    public Item getItem(Long itemId) {
        log.info("Получение вещи по id = {}", itemId);
        if (!itemStorage.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена.");
        }
        return itemStorage.get(itemId);
    }


    @Override
    public Item createItem(Item item) {
        log.info("Создание новой вещи: {}", item);
        idGenerator++;
        item.setId(idGenerator);
        if (itemStorage.containsKey(idGenerator)) {
            idGenerator--;
            throw new ConflictException("Предмет с id " + idGenerator + " уже существует.");
        }
        itemStorage.put(idGenerator, item);
        return item;
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
    public Item updateItem(Item item) {
        log.info("Обновление вещи с id: {}", item.getId());

        Item existingItem = validateItemOwnership(item.getOwnerId(), item.getId());

        updateFields(existingItem, item);

        itemStorage.put(existingItem.getId(), existingItem);
        log.info("Вещь с id: {} успешно обновлена", existingItem.getId());

        return existingItem;
    }

    private void updateFields(Item existingItem, Item newItem) {
        if (newItem.getName() != null && !existingItem.getName().equals(newItem.getName())) {
            log.debug("Обновление имени вещи: '{}' -> '{}'", existingItem.getName(), newItem.getName());
            existingItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !existingItem.getDescription().equals(newItem.getDescription())) {
            log.debug("Обновление описания вещи: '{}' -> '{}'", existingItem.getDescription(), newItem.getDescription());
            existingItem.setDescription(newItem.getDescription());
        }
        if (newItem.isAvailable() != existingItem.isAvailable()) {
            log.debug("Обновление доступности вещи: '{}' -> '{}'", existingItem.isAvailable(), newItem.isAvailable());
            existingItem.setAvailable(newItem.isAvailable());
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
