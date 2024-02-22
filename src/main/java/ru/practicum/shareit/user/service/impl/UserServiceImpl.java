package ru.practicum.shareit.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.ValidationException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage storage;

    public UserServiceImpl(UserStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<UserDto> findAll() {
        log.info("UserService: Происходит обработка запроса на получение всех пользователей");
        return storage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(int userId) {
        log.info("UserService: Происходит обработка запроса на получение пользователя с id {}", userId);
        return UserMapper.toUserDto(storage.findById(userId));
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("UserService: Происходит обработка запроса на создание пользователя с email {}", userDto.getEmail());
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new ValidationException("email пользователя не может быть пустым");
        }
        return UserMapper.toUserDto(storage.create(userDto));
    }

    @Override
    public UserDto update(UserDto userDto, int userId) {
        log.info("UserService: Происходит обработка запроса на обновление пользователя с id {}", userId);
        return UserMapper.toUserDto(storage.update(userDto, userId));
    }

    @Override
    public void delete(int userId) {
        log.info("UserService: Происходит обработка запроса на удаление пользователя с id {}", userId);
        storage.delete(userId);
    }
}