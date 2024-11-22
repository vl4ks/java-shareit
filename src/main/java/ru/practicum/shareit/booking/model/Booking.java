package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Long id;
    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала бронирования должна быть в будущем")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования обязательна")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;
    @NotNull(message = "Вещь для бронирования обязательна")
    private Item item;
    @NotNull(message = "Пользователь,который осуществляет бронирование, должен быть указан")
    private User booker;
    @NotNull(message = "Статус бронирования должен быть указан")
    private BookingStatus status;
}
