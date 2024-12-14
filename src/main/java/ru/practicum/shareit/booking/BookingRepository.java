package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    @Query("select b from Booking b " +
            "where b.booker.id = :bookerId " +
            "and b.start < :now " +
            "and b.end > :now " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByBooker(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, BookingStatus status);

    List<Booking> findAllByItemIdInOrderByStartDesc(List<Long> itemIds);

    @Query("select b from Booking b " +
            "where b.item.id in :itemIds " +
            "and b.start <= :now " +
            "and b.end >= :now order by b.start DESC")
    List<Booking> findCurrentBookingsByOwner(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    List<Booking> findAllByItemIdInAndEndBeforeOrderByStartDesc(List<Long> itemIds, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStartAfterOrderByStartDesc(List<Long> itemIds, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = :bookerId " +
            "and b.status = :status " +
            "order by b.start desc ")
    List<Booking> findBookingsByBookerAndStatus(@Param("bookerId") Long bookerId, @Param("status") BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.owner.id = :ownerId " +
            "and b.end < :now " +
            "order by b.end desc ")
    List<Booking> findLastBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner.id = :ownerId " +
            "and b.start > :now " +
            "order by b.start asc ")
    List<Booking> findNextBookingsByOwner(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            " where " +
            "b.item.id = :itemId " +
            "and b.booker.id = :bookerId " +
            "and b.status= :status " +
            "and b.end<:end ")
    List<Booking> findByItemIdAndBookerIdAndStatusAndEndIsBefore(Long itemId, Long bookerId, BookingStatus status, LocalDateTime end);
}
