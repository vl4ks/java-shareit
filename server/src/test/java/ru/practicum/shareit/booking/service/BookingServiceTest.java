package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookingServiceTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void testCreateBooking_ShouldSaveBookingToDatabase() {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "booker@email.com"));

        Item item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build());

        BookingRequestDto requestDto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingDto bookingDto = bookingService.createBooking(booker.getId(), requestDto);

        Optional<Booking> savedBooking = bookingRepository.findById(bookingDto.getId());
        assertTrue(savedBooking.isPresent());
        assertEquals(BookingStatus.WAITING, savedBooking.get().getStatus());
    }

    @Test
    void testCreateBooking_ThrowValidationException_WhenUserBooksOwnItem() {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        Item item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build());
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(owner.getId(), bookingRequestDto);
        });

        assertEquals("Нельзя забронировать свою собственную вещь.", exception.getMessage());
    }

    @Test
    void createBooking_ShouldThrowValidationException_WhenItemIsNotAvailable() {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        User user = userRepository.save(new User(null, "User", "user@email.com"));

        Item item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(false)
                .owner(owner)
                .build());

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user.getId(), bookingRequestDto);
        });

        assertEquals("Вещь недоступна для бронирования.", exception.getMessage());
    }

    @Test
    void createBooking_ShouldThrowValidationException_WhenBookingTimeIsInvalid() {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        User user = userRepository.save(new User(null, "User", "user@email.com"));

        Item item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build());
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(user.getId(), bookingRequestDto);
        });

        assertEquals("Время начала бронирования должно быть раньше времени окончания.", exception.getMessage());
    }

    @Test
    void testCreateBooking_ThrowValidationException_WhenBookingOverlaps() {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "booker@email.com"));
        Item item = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build());
        Booking existingBooking = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3), item, booker, BookingStatus.APPROVED));

        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4));
        ValidationException exception = assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), requestDto));

        assertEquals("Бронирование пересекается с уже существующим подтвержденным бронированием.", exception.getMessage());
    }
}
