package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestRequestDto {
    @NotBlank(message = "описание запроса не может быть пустым")
    @Size(max = 512, message = "Длина описания запроса не может превышать 512 символов")
    private String description;
}
