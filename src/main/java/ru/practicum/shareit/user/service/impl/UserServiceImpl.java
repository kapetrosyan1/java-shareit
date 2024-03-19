package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.exception.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage storage;

    @Override
    public List<UserDto> findAll() {
        log.info("UserService: Происходит обработка запроса на получение всех пользователей");
        return storage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long userId) {
        log.info("UserService: Происходит обработка запроса на получение пользователя с id {}", userId);
        return UserMapper.toUserDto(storage.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден")));
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("UserService: Происходит обработка запроса на создание пользователя с email {}", userDto.getEmail());
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new ValidationException("email пользователя не может быть пустым");
        }
        return UserMapper.toUserDto(storage.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        log.info("UserService: Происходит обработка запроса на обновление пользователя с id {}", userId);
        User user = storage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId +
                " не найден"));
        User updatedUser = UserMapper.toUser(userDto);

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        storage.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(Long userId) {
        log.info("UserService: Происходит обработка запроса на удаление пользователя с id {}", userId);
        storage.deleteById(userId);
    }
}