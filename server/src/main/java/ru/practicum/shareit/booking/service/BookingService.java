package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingDto updateBookingStatus(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByState(Long userId, BookingState state);

    List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state);

}
