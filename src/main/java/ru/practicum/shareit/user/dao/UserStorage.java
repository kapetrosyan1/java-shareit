package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User findById(int userId);

    User update(UserDto userDto, int userId);

    User create(UserDto userDto);

    void delete(int userId);
}