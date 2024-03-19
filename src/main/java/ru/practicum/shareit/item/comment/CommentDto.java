package ru.practicum.shareit.item.comment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String text;
    private String authorName;
    private LocalDateTime created;
}
