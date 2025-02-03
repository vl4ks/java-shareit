package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController("bookingController")
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                    @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                          @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        log.info("Обновление статуса бронирования с id = {}", bookingId);
        return bookingService.updateBookingStatus(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(HEADER_USER_ID) Long userId,
                                     @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }


    @GetMapping
    public List<BookingDto> getBookingsByState(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsByState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsForOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsForOwner(ownerId, state);
    }

}
