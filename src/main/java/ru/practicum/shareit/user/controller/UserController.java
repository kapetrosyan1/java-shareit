package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
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
    public List<UserDto> findAll() {
        log.info("UserController: обработка запроса на получение всех пользователей");
        return service.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("UserController: обработка запроса на получение пользователя с id {}", userId);
        return service.findById(userId);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("UserController: обработка запроса на создание пользователя {}}", userDto.toString());
        return service.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Valid @RequestBody UserDto userDto,
                          @PathVariable Long userId) {
        log.info("UserController: обработка запроса на обновление пользователя с id {}", userId);
        return service.update(userDto, userId);
    }

    @DeleteMapping("{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("UserController: обработка запроса на удаление пользователя с id {}", userId);
        service.delete(userId);
    }
}