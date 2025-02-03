package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingTest {

    @Test
    void testGetId() {
        Booking booking = new Booking();
        booking.setId(1L);
        assertEquals(1, booking.getId());
    }

    @Test
    void testGetStart() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 1, 20, 15, 0, 0));
        assertEquals(LocalDateTime.of(2025, 1, 20, 15, 0, 0), booking.getStart());
    }

    @Test
    void testGetEnd() {
        Booking booking = new Booking();
        booking.setEnd(LocalDateTime.of(2025, 1, 24, 12, 0, 0));
        assertEquals(LocalDateTime.of(2025, 1, 24, 12, 0, 0), booking.getEnd());
    }

    @Test
    void testGetItem() {
        Booking booking = new Booking();
        Item item = new Item();
        booking.setItem(item);
        assertEquals(item, booking.getItem());
    }

    @Test
    void testGetBooker() {
        Booking booking = new Booking();
        User booker = new User();
        booking.setBooker(booker);
        assertEquals(booker, booking.getBooker());
    }

    @Test
    void testGetStatus() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void testSetId() {
        Booking booking = new Booking();
        booking.setId(1L);
        assertEquals(1, booking.getId());
    }

    @Test
    void testSetStart() {
        Booking booking = new Booking();
        LocalDateTime start = LocalDateTime.now();
        booking.setStart(start);
        assertEquals(start, booking.getStart());
    }

    @Test
    void testSetEnd() {
        Booking booking = new Booking();
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        booking.setEnd(end);
        assertEquals(end, booking.getEnd());
    }

    @Test
    void testSetItem() {
        Booking booking = new Booking();
        Item item = new Item();
        booking.setItem(item);
        assertEquals(item, booking.getItem());
    }

    @Test
    void testSetBooker() {
        Booking booking = new Booking();
        User booker = new User();
        booking.setBooker(booker);
        assertEquals(booker, booking.getBooker());
    }

    @Test
    void testSetStatus() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.REJECTED);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

}