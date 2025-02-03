package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.service.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.service.BookingMapper.toBookingDto;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Начало создания бронирования: userId={}, itemId={}, start={}, end={}",
                userId,
                bookingRequestDto.getItemId(),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd()
        );

        User booker = findUserById(userId);
        Item item = findItemById(bookingRequestDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с id={} пытается забронировать свою собственную вещь с id={}", userId, item.getId());
            throw new ValidationException("Нельзя забронировать свою собственную вещь.");
        }

        if (!item.getAvailable()) {
            log.warn("Вещь с id={} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        Booking booking = toBooking(bookingRequestDto, booker, item);
        if (!booking.getStart().isBefore(booking.getEnd())) {
            log.warn("Некорректное время бронирования: start={}, end={}", booking.getStart(), booking.getEnd());
            throw new ValidationException("Время начала бронирования должно быть раньше времени окончания.");
        }

        boolean hasOverlap = bookingRepository.existsByItemIdAndStatusAndEndAfterAndStartBefore(
                item.getId(),
                BookingStatus.APPROVED,
                booking.getStart(),
                booking.getEnd()
        );

        if (hasOverlap) {
            log.warn("Бронирование пересекается с уже существующим: itemId={}, start={}, end={}",
                    item.getId(),
                    booking.getStart(),
                    booking.getEnd()
            );
            throw new ValidationException("Бронирование пересекается с уже существующим подтвержденным бронированием.");
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование успешно создано: bookingId={}, status={}", savedBooking.getId(), savedBooking.getStatus());
        return toBookingDto(savedBooking);
    }


    @Override
    public BookingDto updateBookingStatus(Long ownerId, Long bookingId, Boolean approved) {
        log.info("Запрос на изменение статуса бронирования: bookingId={}, ownerId={}, approved={}",
                bookingId,
                ownerId,
                approved
        );
        Booking booking = findBookingById(bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            log.warn("Пользователь с id={} не является владельцем вещи для бронирования с id={}", ownerId, bookingId);
            throw new ValidationException("Только владелец может подтвердить или отклонить бронирование.");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования успешно обновлен: bookingId={}, новый статус={}", savedBooking.getId(), savedBooking.getStatus());
        return toBookingDto(savedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.info("Запрос информации о бронировании: bookingId={}, userId={}", bookingId, userId);
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(userId, booking);
        log.info("Информация о бронировании успешно получена: bookingId={}", bookingId);
        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByState(Long userId, BookingState state) {
        return findBookings(userId, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state) {
        findUserById(ownerId);
        List<Long> itemIds = itemRepository.findAllByOwnerId(ownerId).stream()
                .map(Item::getId)
                .toList();
        if (itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        return findBookingsForOwner(itemIds, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    private List<Booking> findBookings(Long userId, BookingState state) {
        log.info("Запрос бронирований для пользователя: userId={}, state={}", userId, state);
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                log.debug("Поиск текущих бронирований для userId={}", userId);
                return bookingRepository.findCurrentBookingsByBooker(userId, now);
            case PAST:
                log.debug("Поиск завершенных бронирований для userId={}", userId);
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE:
                log.debug("Поиск будущих бронирований для userId={}", userId);
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING:
                log.debug("Поиск ожидающих бронирований для userId={}", userId);
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                log.debug("Поиск отклоненных бронирований для userId={}", userId);
                return bookingRepository.findBookingsByBookerAndStatus(userId, BookingStatus.REJECTED);
            case ALL:
                log.debug("Поиск всех бронирований для userId={}", userId);
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            default:
                log.error("Неизвестное состояние бронирования: state={}", state);
                throw new IllegalArgumentException("Неизвестное состояние бронирования: " + state);
        }
    }

    private List<Booking> findBookingsForOwner(List<Long> itemIds, BookingState state) {
        log.info("Запрос бронирований для владельца: itemIds={}, state={}", itemIds, state);
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                log.debug("Поиск текущих бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findCurrentBookingsByOwner(itemIds, now);
            case PAST:
                log.debug("Поиск завершенных бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findAllByItemIdInAndEndBeforeOrderByStartDesc(itemIds, now);
            case FUTURE:
                log.debug("Поиск будущих бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(itemIds, now);
            case WAITING:
                log.debug("Поиск ожидающих бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.WAITING);
            case REJECTED:
                log.debug("Поиск отклоненных бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.REJECTED);
            case ALL:
                log.debug("Поиск всех бронирований для владельца: itemIds={}", itemIds);
                return bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);
            default:
                log.error("Неизвестное состояние бронирования: state={}", state);
                throw new IllegalArgumentException("Неизвестное состояние бронирования: " + state);
        }
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена."));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено."));
    }

    private void validateBookingAccess(Long userId, Booking booking) {
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("У пользователя нет доступа к бронированию.");
        }
    }
}
