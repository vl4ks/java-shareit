package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.DateTimeStartBeforeEnd;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@DateTimeStartBeforeEnd
public class BookingRequestDto {
	@NotNull
	private long itemId;
	@FutureOrPresent
	private LocalDateTime start;
	@Future
	private LocalDateTime end;
}
