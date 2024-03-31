package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserRequestDto {
    private String name;
    @Email(message = "передан некорректный формат email")
    private String email;
}