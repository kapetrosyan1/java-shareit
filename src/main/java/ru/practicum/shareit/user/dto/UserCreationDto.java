package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserCreationDto {
    private Long id;
    @NotBlank(message = "имя пользователя не может быть пустым")
    private String name;
    @Email(message = "передан некорректный формат email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
