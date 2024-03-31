package ru.practicum.shareit.user;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserIntegrateTest {
    @Autowired
    private UserServiceImpl userService;

    private final EasyRandom easyRandom = new EasyRandom();

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addUser() {
        UserCreationDto creationDto = new UserCreationDto();
        creationDto.setName("user");
        creationDto.setEmail("user@user.com");

        UserResponseDto newUser = userService.create(creationDto);

        UserResponseDto savedUser = userService.findById(newUser.getId());

        assertEquals(newUser.getName(), savedUser.getName());
        assertEquals(newUser.getEmail(), savedUser.getEmail());

        assertThrows(DataIntegrityViolationException.class, () -> userService.create(creationDto));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void findAll() {
        UserCreationDto creationDto = easyRandom.nextObject(UserCreationDto.class);
        UserResponseDto user = userService.create(creationDto);
        assertEquals(1, userService.findAll().size());
        UserResponseDto newUser = userService.create(easyRandom.nextObject(UserCreationDto.class));
        List<UserResponseDto> userList = userService.findAll();

        assertEquals(2, userList.size());
        assertEquals(user.getId(), userList.get(0).getId());
        assertEquals(newUser.getId(), userList.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testFindById() {
        assertThrows(NotFoundException.class, () -> userService.findById(1L));

        UserCreationDto creationDto = easyRandom.nextObject(UserCreationDto.class);

        userService.create(creationDto);

        UserResponseDto newUser = userService.create(easyRandom.nextObject(UserCreationDto.class));

        assertEquals(newUser, userService.findById(newUser.getId()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testUpdate() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("updateName");

        assertThrows(NotFoundException.class, () -> userService.update(requestDto, 1L));

        UserResponseDto user = userService.create(easyRandom.nextObject(UserCreationDto.class));

        UserResponseDto responseDto = userService.update(requestDto, user.getId());

        assertEquals(requestDto.getName(), responseDto.getName());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testDelete() {
        UserResponseDto responseDto = userService.create(easyRandom.nextObject(UserCreationDto.class));

        assertEquals(1, userService.findAll().size());

        userService.delete(responseDto.getId());

        assertEquals(0, userService.findAll().size());
    }
}
