package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 400)
    private String description;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "requestor_id")
    private User requester;

    @CreationTimestamp
    private LocalDateTime created = LocalDateTime.now();

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY)
    private List<Item> items;

    public ItemRequest(Long id, String description, User user, LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.requester = user;
        this.created = created;
    }

}
