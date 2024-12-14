package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String text;
    @NotBlank(message = "Имя автора не может быть пустым")
    private String authorName;

    private LocalDateTime created;
}
