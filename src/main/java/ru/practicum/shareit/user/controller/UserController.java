package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService service;

    @GetMapping
    public List<UserResponseDto> findAll() {
        log.info("UserController: обработка запроса на получение всех пользователей");
        return service.findAll();
    }

    @GetMapping("/{userId}")
    public UserResponseDto findById(@PathVariable Long userId) {
        log.info("UserController: обработка запроса на получение пользователя с id {}", userId);
        return service.findById(userId);
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreationDto userCreationDto) {
        log.info("UserController: обработка запроса на создание пользователя {}}", userCreationDto.toString());
        return service.create(userCreationDto);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@Valid @RequestBody UserRequestDto userRequestDto,
                                  @PathVariable Long userId) {
        log.info("UserController: обработка запроса на обновление пользователя с id {}", userId);
        return service.update(userRequestDto, userId);
    }

    @DeleteMapping("{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("UserController: обработка запроса на удаление пользователя с id {}", userId);
        service.delete(userId);
    }
}