package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.CommentMapper.*;
import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItem(Long itemId) {
        log.info("Получение вещи по id = {}", itemId);
        Item item = validateItemExists(itemId);

        List<Comment> comments = commentRepository.findByItemId(itemId);

        ItemDto itemDto = toItemDto(item);
        itemDto.setComments(toListDto(comments));
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsWithBookings(Long ownerId) {
        log.info("Получение всех вещей владельца с id = {}", ownerId);

        List<Item> items = itemRepository.findAllByOwnerId(ownerId);

        if (items.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        Map<Long, Booking> lastBookings = bookingRepository.findLastBookingsByOwner(ownerId, now)
                .stream().collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));
        Map<Long, Booking> nextBookings = bookingRepository.findNextBookingsByOwner(ownerId, now)
                .stream().collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));

        List<Comment> comments = commentRepository.findByItem_Owner_Id(ownerId);

        Map<Long, List<CommentDto>> commentsByItemId = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())));

        return items.stream()
                .map(item -> {
                    Long itemId = item.getId();
                    List<CommentDto> commentsForItem = commentsByItemId.getOrDefault(itemId, List.of());
                    return toItemDto(item, lastBookings.get(itemId), nextBookings.get(itemId), commentsForItem);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Создание новой вещи пользователем с id = {}", userId);
        User owner = validateUser(userId);
        Item newItem = toItem(itemDto, null, null, owner);
        Item savedItem = itemRepository.save(newItem);
        return toItemDto(savedItem);
    }


    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentCreateDto createCommentDto) {
        log.info("Добавление комментария для вещи с id = {} пользователем с id = {}", itemId, userId);

        Item item = validateItemExists(itemId);
        User user = validateUser(userId);

        Booking booking = bookingRepository
                .findByItemIdAndBookerIdAndStatusAndEndIsBefore(itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("Бронирование не найдено или не завершено."));

        log.info("Бронирование найдено: id = {}", booking.getId());

        Comment comment = toComment(createCommentDto, user, item, LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return toCommentDto(savedComment);

    }


    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.info("Поиск вещей по тексту: {}", text);
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        log.info("Обновление вещи с id = {} пользователем с id = {}", itemDto.getId(), userId);

        validateUser(userId);

        Item existingItem = validateItemExists(itemDto.getId());

        validateItemOwnership(existingItem, userId);

        updateFields(existingItem, itemDto);

        Item updatedItem = itemRepository.save(existingItem);

        log.info("Вещь с id = {} успешно обновлена", updatedItem.getId());
        return toItemDto(updatedItem);
    }


    @Override
    @Transactional
    public void deleteItem(User user, Long itemId) {
        log.info("Удаление вещи с id = {} пользователем с id = {}", itemId, user.getId());
        Item item = validateItemExists(itemId);
        validateItemOwnership(item, user.getId());
        itemRepository.delete(item);
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Item validateItemExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    private void validateItemOwnership(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Доступ запрещён. Вы не являетесь владельцем вещи с id = " + item.getId());
        }
    }

    private void updateFields(Item existingItem, ItemDto newItem) {
        if (newItem.getName() != null && !newItem.getName().equals(existingItem.getName())) {
            log.debug("Обновление имени вещи: '{}' -> '{}'", existingItem.getName(), newItem.getName());
            existingItem.setName(newItem.getName());
        }

        if (newItem.getDescription() != null && !newItem.getDescription().equals(existingItem.getDescription())) {
            log.debug("Обновление описания вещи: '{}' -> '{}'", existingItem.getDescription(), newItem.getDescription());
            existingItem.setDescription(newItem.getDescription());
        }

        if (newItem.getAvailable() != null && Boolean.parseBoolean(newItem.getAvailable()) != existingItem.isAvailable()) {
            log.debug("Обновление доступности вещи: '{}' -> '{}'", existingItem.isAvailable(), newItem.getAvailable());
            existingItem.setAvailable(Boolean.parseBoolean(newItem.getAvailable()));
        }
    }
}
