package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> findAll();

    UserResponseDto findById(Long userId);

    UserResponseDto create(UserCreationDto userCreationDto);

    UserResponseDto update(UserRequestDto userRequestDto, Long userId);

    void delete(Long userId);
}