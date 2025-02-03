package ru.practicum.shareit.booking.controller;

import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;
	private static final String HEADER_USER_ID = "X-Sharer-User-Id";

	@PostMapping
	public ResponseEntity<Object> addBooking(@RequestHeader(HEADER_USER_ID) Long userId,
											 @RequestBody @Valid BookingRequestDto bookingRequestDto) {
		return bookingClient.createBooking(userId, bookingRequestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBookingStatus(@RequestHeader(HEADER_USER_ID) Long ownerId,
												@PathVariable Long bookingId,
												@RequestParam Boolean approved) {
		log.info("Обновление статуса бронирования с id = {}", bookingId);
		return bookingClient.updateBookingStatus(ownerId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_USER_ID) Long userId,
											 @PathVariable Long bookingId) {
		return bookingClient.getBookingById(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllUserBookings(@RequestHeader(HEADER_USER_ID) Long userId,
													 @RequestParam(defaultValue = "ALL") String state,
													 @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
													 @RequestParam(defaultValue = "10") @Positive Long size) {
		BookingState bookingState = getBookingState(state);
		return bookingClient.getAllUserBookings(userId, bookingState, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllUserItemsBookings(@RequestHeader(HEADER_USER_ID) Long userId,
														  @RequestParam(defaultValue = "ALL") String state) {
		BookingState bookingState = getBookingState(state);

		return bookingClient.getAllUserItemsBookings(userId, bookingState);
	}

	private BookingState getBookingState(String state) {
		return BookingState.from(state)
				.orElseThrow(() -> new ValidationException("State имеет неизвестное значение."));
	}

}
