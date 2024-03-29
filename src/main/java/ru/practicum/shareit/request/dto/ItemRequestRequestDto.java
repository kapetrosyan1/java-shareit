package ru.practicum.shareit.request.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ItemRequestRequestDto {
    @NotBlank(message = "описание запроса не может быть пустым")
    @Size(max = 512, message = "Длина описания запроса не может превышать 512 символов")
    private String description;
}
