package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = findUserById(userId);
        Item item = findItemById(bookingRequestDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Нельзя забронировать свою собственную вещь.");
        }

        if (!item.isAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        Booking booking = toBooking(bookingRequestDto, booker, item);
        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new ValidationException("Начало и конец бронирования не могут быть пустыми.");
        }
        Booking savedBooking = bookingRepository.save(booking);

        return toBookingDto(savedBooking);
    }

    @Override
    public BookingDto updateBookingStatus(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может подтвердить или отклонить бронирование.");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Только бронирования в статусе WAITING могут быть обновлены.");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(userId, booking);
        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByState(Long userId, BookingState state) {
        User user = findUserById(userId);
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
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                return bookingRepository.findCurrentBookingsByBooker(userId, now);
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findBookingsByBookerAndStatus(userId, BookingStatus.REJECTED);
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            default:
                throw new IllegalArgumentException("Неизвестное состояние бронирования: " + state);
        }
    }

    private List<Booking> findBookingsForOwner(List<Long> itemIds, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                return bookingRepository.findCurrentBookingsByOwner(itemIds, now);
            case PAST:
                return bookingRepository.findAllByItemIdInAndEndBeforeOrderByStartDesc(itemIds, now);
            case FUTURE:
                return bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(itemIds, now);
            case WAITING:
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.REJECTED);
            case ALL:
                return bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);
            default:
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
