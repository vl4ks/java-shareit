package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.service.ItemMapper.toItem;
import static ru.practicum.shareit.item.service.ItemMapper.toItemDto;

@Slf4j(topic = "ItemServiceImpl")
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepo;

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItem(Long itemId) {
        log.info("Получение вещи по id = {}", itemId);
        Item item = validateItemExists(itemId);

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentDtos = CommentMapper.toListDto(comments);

        ItemDto itemDto = toItemDto(item);
        itemDto.setComments(commentDtos);
        log.info("Получена вещь - {}", itemDto);
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
    public ItemDto createItem(Long userId, ItemSaveDto itemSaveDto) {
        log.info("Создание новой вещи пользователем с id = {}, вещь - {}", userId, itemSaveDto);
        User owner = validateUser(userId);
        ItemRequest request = null;
        if (itemSaveDto.getRequestId() != null) {
            request = itemRequestRepo.findById(itemSaveDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id = " + itemSaveDto.getRequestId() + " не найден"));
        }

        Item newItem = toItem(itemSaveDto, null, request, owner);
        newItem.setOwner(owner);
        newItem.setRequest(request);
        Item savedItem = itemRepository.save(newItem);
        log.info("Добавлена вещь пользователем {}, вещь - {}", userId, savedItem);
        log.info("Сохраненная вещь с id = {}", savedItem.getId());
        return toItemDto(savedItem);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentSaveDto commentSaveDto) {
        log.info("Добавление комментария пользователем с id = {} для вещи с id = {} ", userId, itemId);
        User user = validateUser(userId);
        Item item = validateItemExists(itemId);
        List<Booking> bookings = bookingRepository
                .findByItemIdAndBookerIdAndStatusAndEndIsBefore(itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!bookings.isEmpty()) {
            Comment comment = new Comment();
            comment.setText(commentSaveDto.getText());
            comment.setItem(item);
            comment.setAuthor(user);
            comment.setCreated(LocalDateTime.now());
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new ValidationException("Пользователь не бронировал эту вещь");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.info("Поиск вещей по тексту: {}", text);
        return itemRepository.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemSaveDto itemDto, Long userId, Long itemId) {
        log.info("Обновление вещи с id = {} пользователем с id = {}", itemId, userId);

        validateUser(userId);

        Item existingItem = validateItemExists(itemId);

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

    private void updateFields(Item existingItem, ItemSaveDto newItem) {
        if (newItem.getName() != null && !newItem.getName().equals(existingItem.getName())) {
            log.debug("Обновление имени вещи: '{}' -> '{}'", existingItem.getName(), newItem.getName());
            existingItem.setName(newItem.getName());
        }

        if (newItem.getDescription() != null && !newItem.getDescription().equals(existingItem.getDescription())) {
            log.debug("Обновление описания вещи: '{}' -> '{}'", existingItem.getDescription(), newItem.getDescription());
            existingItem.setDescription(newItem.getDescription());
        }

        if (newItem.getAvailable() != null && !newItem.getAvailable().equals(existingItem.getAvailable())) {
            log.debug("Обновление доступности вещи: '{}' -> '{}'", existingItem.getAvailable(), newItem.getAvailable());
            existingItem.setAvailable(newItem.getAvailable());
        }

        if (newItem.getRequestId() != null) {
            var request = itemRequestRepo.findById(newItem.getRequestId()).orElseThrow(
                    () -> new NotFoundException("Запрос с ID " + newItem.getRequestId() + " не найден")
            );
            existingItem.setRequest(request);
        }
    }
}
