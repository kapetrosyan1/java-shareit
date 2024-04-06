package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("USER_GATEWAY: Get all users");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable @Positive long userId) {
        log.info("USER_GATEWAY: Get user by userId={}", userId);
        return userClient.getUser(userId);
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@RequestBody @Valid UserCreationDto userCreationDto) {
        log.info("USER_GATEWAY: Create new user {}", userCreationDto);
        return userClient.addUser(userCreationDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserRequestDto userRequestDto,
                                             @PathVariable @Positive long userId) {
        log.info("USER_GATEWAY: Update user userId={} by data {}", userId, userRequestDto);
        return userClient.updateUser(userRequestDto, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive long userId) {
        log.info("USER_GATEWAY: delete user userId={}", userId);
        return userClient.deleteUser(userId);
    }
}