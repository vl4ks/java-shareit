package ru.practicum.shareit.booking.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Entity
@Table(name = "bookings")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDateTime start;

    @Column(name = "end_date")
    private LocalDateTime end;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "booker_id")
    private User booker;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 10)
    private BookingStatus status;
}
