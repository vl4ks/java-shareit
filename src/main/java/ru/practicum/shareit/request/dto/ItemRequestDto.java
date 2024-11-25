package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Описание запроса не должно быть пустым")
    private String description;
    @NotNull(message = "Создатель запроса должен быть указан")
    private User requester;
    @NotNull(message = "Дата создания запроса обязательна")
    private LocalDateTime created;
}
