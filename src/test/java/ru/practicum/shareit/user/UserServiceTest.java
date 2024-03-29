package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("user");
        user.setEmail("user@user.com");
    }

    @Test
    void testCreateUser() {
        UserCreationDto userDto = new UserCreationDto();
        userDto.setName("user");
        userDto.setEmail("user@user.com");

        when(userStorage.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDto = userService.create(userDto);

        assertEquals(UserMapper.toUserResponseDto(user).toString(), responseDto.toString());
        verify(userStorage, times(1)).save(any(User.class));
    }

    @Test
    void testCreateFailDuplicateEmail() {
        UserCreationDto userDto = new UserCreationDto();
        userDto.setName("user");
        userDto.setEmail("user@user.com");

        when(userStorage.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));
        verify(userStorage, times(1)).save(any());
    }

    @Test
    void testFindById() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));

        UserResponseDto responseDto = userService.findById(1L);
        verify(userStorage, times(1)).findById(1L);
        assertEquals(UserMapper.toUserResponseDto(user).toString(), responseDto.toString());
    }

    @Test
    void testFindByIdFailNotFound() {
        when(userStorage.findById(anyLong())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findById(user.getId()));
        assertEquals(String.format("Пользователь с id %d не найден", user.getId()), exception.getMessage());
    }

    @Test
    void testUpdate() {
        UserRequestDto updatedUser = new UserRequestDto();
        updatedUser.setId(1L);
        updatedUser.setName("update");
        updatedUser.setEmail("update@update.com");

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.save(any(User.class))).thenReturn(UserMapper.toUser(updatedUser));

        UserResponseDto userResponseDto = userService.update(updatedUser, 1L);
        verify(userStorage, times(1)).save(any());
        assertEquals(updatedUser.getName(), userResponseDto.getName());
        assertEquals(updatedUser.getEmail(), userResponseDto.getEmail());
        assertEquals(updatedUser.getId(), userResponseDto.getId());
    }

    @Test
    void testFindAll() {
        User user1 = new User();
        user1.setId(2L);
        user1.setName("user1");
        user1.setEmail("user1@user.com");
        when(userStorage.findAll()).thenReturn(List.of(user, user1));
        List<UserResponseDto> responseDtoList = userService.findAll();
        verify(userStorage, times(1)).findAll();
        assertEquals(List.of(UserMapper.toUserResponseDto(user), UserMapper.toUserResponseDto(user1)), responseDtoList);
    }

    @Test
    void testDelete() {
        userService.delete(1L);
        verify(userStorage, times(1)).deleteById(1L);
    }
}