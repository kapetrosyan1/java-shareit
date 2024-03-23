package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage storage;

    @Override
    public List<UserResponseDto> findAll() {
        log.info("UserService: Происходит обработка запроса на получение всех пользователей");
        return storage.findAll().stream()
                .map(UserMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto findById(Long userId) {
        log.info("UserService: Происходит обработка запроса на получение пользователя с id {}", userId);
        return UserMapper.toUserResponseDto(storage.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id %d не найден", userId))));
    }

    @Override
    @Transactional
    public UserResponseDto create(UserCreationDto userCreationDto) {
        log.info("UserService: Происходит обработка запроса на создание пользователя с email {}", userCreationDto.getEmail());
        return UserMapper.toUserResponseDto(storage.save(UserMapper.fromUserCreationDto(userCreationDto)));
    }

    @Override
    @Transactional
    public UserResponseDto update(UserRequestDto userRequestDto, Long userId) {
        log.info("UserService: Происходит обработка запроса на обновление пользователя с id {}", userId);
        User user = storage.findById(userId).orElseThrow(() -> new NotFoundException(String.format(
                "Пользователь с id %d не найден", userId)));
        User updatedUser = UserMapper.toUser(userRequestDto);

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        storage.save(user);
        return UserMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        log.info("UserService: Происходит обработка запроса на удаление пользователя с id {}", userId);
        storage.deleteById(userId);
    }
}