package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveDto {
    @NotBlank(message = "Необходимо указать имя", groups = {ValidationGroups.Create.class})
    @Size(min = 1, max = 50, message = "Имя должно быть от 1 до 50 символов.", groups = {ValidationGroups.Create.class,
            ValidationGroups.Update.class})
    private String name;

    @NotBlank(message = "Email не может быть пустым", groups = {ValidationGroups.Create.class})
    @Email(message = "Некорректный email", groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Size(min = 1, max = 150, message = "Email должен содержать от 1 до 150 символов.", groups = {ValidationGroups.Create.class,
            ValidationGroups.Update.class})
    private String email;
}
