package ru.practicum.shareit.request.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Long id;
    @NotBlank(message = "Описание запроса не должно быть пустым")
    private String description;
    @NotNull(message = "Создатель запроса должен быть указан")
    private User requester;
    @NotNull(message = "Дата создания запроса обязательна")
    private LocalDateTime created;
}
