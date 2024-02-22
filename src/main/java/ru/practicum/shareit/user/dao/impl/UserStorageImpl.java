package ru.practicum.shareit.user.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.exceptions.AlreadyExistException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserStorageImpl implements UserStorage {
    private final Map<Integer, User> storage = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();
    int id = 1;

    @Override
    public List<User> findAll() {
        log.info("UserStorage: получен запрос на поиск всех пользователей");
        return new ArrayList<>(storage.values());
    }

    @Override
    public User findById(int userId) {
        log.info("UserStorage: получен запрос на поиск пользователя с id {}", userId);
        if (!storage.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return storage.get(userId);
    }

    @Override
    public User update(UserDto userDto, int userId) {
        log.info("UserStorage: получен запрос на обновление пользователя с id {}", userId);
        if (!storage.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (emailSet.contains(userDto.getEmail()) && !storage.get(userId).getEmail().equals(userDto.getEmail())) {
            throw new AlreadyExistException("Данный email уже зарегистрирован");
        }
        User updatedUser = storage.get(userId);
        if (userDto.getName() != null) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            emailSet.remove(updatedUser.getEmail());
            updatedUser.setEmail(userDto.getEmail());
            emailSet.add(updatedUser.getEmail());
        }
        storage.put(userId, updatedUser);
        log.info("UserStorage: Пользователь с id {} успешно обновлен", userId);
        return updatedUser;
    }

    @Override
    public User create(UserDto userDto) {
        log.info("UserStorage: получен запрос на добавление пользователя с email {}", userDto.getEmail());
        if (emailSet.contains(userDto.getEmail())) {
            throw new AlreadyExistException("Пользователь с email " + userDto.getEmail() + " уже зарегистрирован");
        }
        emailSet.add(userDto.getEmail());
        userDto.setId(id);
        storage.put(id, UserMapper.toUser(userDto));
        id++;
        return UserMapper.toUser(userDto);
    }

    @Override
    public void delete(int userId) {
        log.info("UserStorage: получен запрос на удаление пользователя с id {}", userId);
        if (!storage.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        emailSet.remove(storage.get(userId).getEmail());
        storage.remove(userId);
    }
}