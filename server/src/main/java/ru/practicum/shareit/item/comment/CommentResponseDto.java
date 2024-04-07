package ru.practicum.shareit.item.comment;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private String authorName;
    @EqualsAndHashCode.Exclude
    private LocalDateTime created;
}
