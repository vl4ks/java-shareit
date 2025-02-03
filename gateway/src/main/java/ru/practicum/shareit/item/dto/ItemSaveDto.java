package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemSaveDto {
    @NotNull(message = "Название не может быть пустым.", groups = {ValidationGroups.Create.class})
    @Size(min = 1, max = 30, message = "Название не может быть длиннее 30 символов.", groups = {ValidationGroups.Create.class,
            ValidationGroups.Update.class})
    String name;

    @NotNull(message = "Описание не может быть пустым.", groups = {ValidationGroups.Create.class})
    @Size(min = 1, max = 400, message = "Описание не может быть длиннее 400 символов.", groups = {ValidationGroups.Create.class,
            ValidationGroups.Update.class})
    String description;

    @NotNull(message = "Доступность не может быть пустым.", groups = {ValidationGroups.Create.class})
    Boolean available;

    Long requestId;
}
