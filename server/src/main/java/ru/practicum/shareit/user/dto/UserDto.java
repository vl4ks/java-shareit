package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;
    @NotBlank(message = "Адрес электронной почты должен быть указан")
    private String email;

}
