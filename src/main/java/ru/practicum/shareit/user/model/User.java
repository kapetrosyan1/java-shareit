package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class User {
    private int id;
    private String name;
    @Email(message = "Передан некорректный формат email")
    @NotBlank
    private String email;
}