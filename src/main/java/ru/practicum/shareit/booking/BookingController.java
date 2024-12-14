package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.*;

import java.util.List;

@RestController
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
                                          @RequestParam boolean approved) {
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
