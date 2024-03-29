package ru.practicum.shareit.item.comment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CommentRequestDto {
    @NotBlank
    @Size(max = 512)
    private String text;
}
