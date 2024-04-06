package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
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

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addUser() {
        UserRequestDto creationDto = new UserRequestDto();
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
        UserRequestDto creationDto = new UserRequestDto();
        creationDto.setName("user");
        creationDto.setEmail("user@user.com");
        UserResponseDto user = userService.create(creationDto);
        assertEquals(1, userService.findAll().size());
        UserRequestDto newCreationDto = new UserRequestDto();
        newCreationDto.setName("newUser");
        newCreationDto.setEmail("newUser@user.com");
        UserResponseDto newUser = userService.create(newCreationDto);
        List<UserResponseDto> userList = userService.findAll();

        assertEquals(2, userList.size());
        assertEquals(user.getId(), userList.get(0).getId());
        assertEquals(newUser.getId(), userList.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testFindById() {
        assertThrows(NotFoundException.class, () -> userService.findById(1L));

        UserRequestDto creationDto = new UserRequestDto();
        creationDto.setName("user");
        creationDto.setEmail("user@user.com");

        userService.create(creationDto);

        UserRequestDto newCreationDto = new UserRequestDto();
        newCreationDto.setName("newUser");
        newCreationDto.setEmail("newUser@user.com");
        UserResponseDto newUser = userService.create(newCreationDto);

        assertEquals(newUser, userService.findById(newUser.getId()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testUpdate() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("updateName");

        assertThrows(NotFoundException.class, () -> userService.update(requestDto, 1L));

        UserRequestDto creationDto = new UserRequestDto();
        creationDto.setName("user");
        creationDto.setEmail("user@user.com");

        UserResponseDto user = userService.create(creationDto);

        UserResponseDto responseDto = userService.update(requestDto, user.getId());

        assertEquals(requestDto.getName(), responseDto.getName());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testDelete() {
        UserRequestDto creationDto = new UserRequestDto();
        creationDto.setName("user");
        creationDto.setEmail("user@user.com");

        UserResponseDto responseDto = userService.create(creationDto);

        assertEquals(1, userService.findAll().size());

        userService.delete(responseDto.getId());

        assertEquals(0, userService.findAll().size());
    }
}
