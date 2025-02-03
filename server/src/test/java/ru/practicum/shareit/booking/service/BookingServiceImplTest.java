package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private final User user1 = new User(1L, "User1", "user1@email.com");
    private final User user2 = new User(2L, "User2", "user2@email.com");
    private final UserDto userDto = new UserDto(1L, "User", "user@email.com");
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .build();
    private final Item item1 = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(user1)
            .build();
    private final Item item2 = Item.builder()
            .id(1L)
            .name("Item2")
            .description("Description2")
            .available(false)
            .owner(user1)
            .build();

    @Test
    void testCreateBooking_ReturnBookingDto_whenBookingIsSuccessful() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        User user2 = new User(2L, "User2", "user2@email.com");
        item1.setOwner(user2);

        Booking savedBooking = new Booking(1L, bookingRequestDto.getStart(), bookingRequestDto.getEnd(), item1, user1, BookingStatus.WAITING);

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(savedBooking);

        BookingDto result = bookingService.createBooking(user1.getId(), bookingRequestDto);

        assertNotNull(result);
        assertEquals(savedBooking.getId(), result.getId());
        assertEquals(savedBooking.getStart(), result.getStart());
        assertEquals(savedBooking.getEnd(), result.getEnd());
        assertEquals(savedBooking.getItem().getId(), result.getItem().getId());
        assertEquals(savedBooking.getBooker().getId(), result.getBooker().getId());

        Mockito.verify(userRepository).findById(user1.getId());
        Mockito.verify(itemRepository).findById(item1.getId());
        Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
    }

    @Test
    void testCreateBooking_ThrowValidationException_whenBookerTriesToBookOwnItem() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user1.getId(), bookingRequestDto);
        });

        assertEquals("Нельзя забронировать свою собственную вещь.", exception.getMessage());
    }

    @Test
    void testCreateBooking_ThrowValidationException_whenItemIsNotAvailable() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item2.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));


        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user2.getId(), bookingRequestDto);
        });

        assertEquals("Вещь недоступна для бронирования.", exception.getMessage());
    }

    @Test
    void testCreateBooking_ThrowValidationException_whenStartTimeIsAfterEndTime() {

        BookingRequestDto bookingRequestDto = new BookingRequestDto(item1.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user2.getId(), bookingRequestDto);
        });

        assertEquals("Время начала бронирования должно быть раньше времени окончания.", exception.getMessage());
    }

    @Test
    void testCreateBooking_ThrowValidationException_whenBookingTimesOverlap() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.existsByItemIdAndStatusAndEndAfterAndStartBefore(item1.getId(), BookingStatus.APPROVED, bookingRequestDto.getStart(), bookingRequestDto.getEnd()))
                .thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user2.getId(), bookingRequestDto);
        });

        assertEquals("Бронирование пересекается с уже существующим подтвержденным бронированием.", exception.getMessage());
    }

    @Test
    void testUpdateBookingStatus_RejectBooking_whenOwnerRejects() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);
        Booking updatedBooking = new Booking(1L, booking.getStart(), booking.getEnd(), item1, user1, BookingStatus.REJECTED);

        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(updatedBooking);

        BookingDto result = bookingService.updateBookingStatus(item1.getOwner().getId(), booking.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
    }

    @Test
    void testUpdateBookingStatus_ThrowValidationException_whenUserIsNotOwner() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.updateBookingStatus(user2.getId(), booking.getId(), true)
        );

        assertEquals("Только владелец может подтвердить или отклонить бронирование.", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void testUpdateBookingStatus_ThrowNotFoundException_whenBookingNotFound() {
        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBookingStatus(item1.getOwner().getId(), 99L, true)
        );

        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }


    @Test
    void testGetBookingById_ReturnBookingDto_whenUserHasAccess() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);
        BookingDto expectedBookingDto = new BookingDto(1L, booking.getStart(), booking.getEnd(), BookingStatus.WAITING, userDto, itemDto);

        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(user1.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(expectedBookingDto.getId(), result.getId());
        assertEquals(expectedBookingDto.getStart(), result.getStart());
        assertEquals(expectedBookingDto.getEnd(), result.getEnd());
        assertEquals(expectedBookingDto.getStatus(), result.getStatus());
        assertEquals(expectedBookingDto.getBooker().getId(), result.getBooker().getId());
        assertEquals(expectedBookingDto.getItem().getId(), result.getItem().getId());

        Mockito.verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingsByState_ReturnBookings_whenStateIsAll() {
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item2, user1, BookingStatus.APPROVED)
        );

        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(user1.getId()))
                .thenReturn(bookings);

        List<BookingDto> result = bookingService.getBookingsByState(user1.getId(), BookingState.ALL);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());

        Mockito.verify(bookingRepository).findAllByBookerIdOrderByStartDesc(user1.getId());
    }


    @Test
    void testGetBookingsForOwner_ReturnBookings_whenStateIsFuture() {
        List<Long> itemIds = List.of(item1.getId(), item2.getId());
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user2, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item2, user2, BookingStatus.APPROVED)
        );

        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        Mockito.when(itemRepository.findAllByOwnerId(user1.getId()))
                .thenReturn(List.of(item1, item2));
        Mockito.when(bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());

        Mockito.verify(userRepository).findById(user1.getId());
        Mockito.verify(itemRepository).findAllByOwnerId(user1.getId());
        Mockito.verify(bookingRepository).findAllByItemIdInAndStartAfterOrderByStartDesc(Mockito.eq(itemIds), Mockito.any(LocalDateTime.class));
    }

    @Test
    void testGetBookingsForOwner_ReturnEmptyList_whenNoItemsExist() {
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        Mockito.when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(Collections.emptyList());

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        Mockito.verify(userRepository).findById(user1.getId());
        Mockito.verify(itemRepository).findAllByOwnerId(user1.getId());
        Mockito.verifyNoInteractions(bookingRepository);
    }
}
