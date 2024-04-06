package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDto {
    @NotBlank(message = "имя пользователя не может быть пустым")
    private String name;
    @Email(message = "передан некорректный формат email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
