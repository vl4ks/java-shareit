package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserMapper;

public final class BookingMapper {
    private BookingMapper() {
    }

    public static Booking toBooking(BookingRequestDto dto, User booker, Item item) {
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();
    }
    public static Booking toBooking(BookingRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .build();
    }


    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .build();
    }

}
