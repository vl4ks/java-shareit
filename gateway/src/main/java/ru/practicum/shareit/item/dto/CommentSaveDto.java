package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentSaveDto {
    @NotNull(message = "Комментарий не может быть пустым")
    @Size(min = 1, max = 400, message = "Комментарий должен содержать от 1 до 400 символов")
    private String text;

}
