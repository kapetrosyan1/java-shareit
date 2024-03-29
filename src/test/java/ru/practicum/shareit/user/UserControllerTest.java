package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mvc;
    @MockBean
    UserService userService;

    private static UserCreationDto userCreationDto;
    private static UserRequestDto userRequestDto;
    private static UserResponseDto userResponseDto;

    @BeforeAll
    public static void setUp() {
        userCreationDto = new UserCreationDto();
        userCreationDto.setName("user");
        userCreationDto.setEmail("user@user.com");

        userRequestDto = new UserRequestDto();
        userRequestDto.setName("user");
        userRequestDto.setEmail("user@user.com");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("user");
        userResponseDto.setEmail("user@user.com");
    }

    @Test
    void testSaveUser() throws Exception {
        when(userService.create(any(UserCreationDto.class)))
                .thenReturn(userResponseDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userCreationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())))
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())));
    }

    @Test
    void testSaveUserFailEmail() throws Exception {
        UserCreationDto userCreationDto1 = new UserCreationDto();
        userCreationDto1.setName("name");
        userCreationDto1.setEmail("email");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userCreationDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveUserFailName() throws Exception {
        UserCreationDto userCreationDto1 = new UserCreationDto();
        userCreationDto1.setName("");
        userCreationDto1.setEmail("email");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userCreationDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.update(any(UserRequestDto.class), anyLong())).thenReturn(userResponseDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())))
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())));
    }

    @Test
    void testFindAll() throws Exception {
        UserResponseDto userResponseDto1 = new UserResponseDto();
        userResponseDto1.setId(2L);

        when(userService.findAll()).thenReturn(List.of(userResponseDto, userResponseDto1));

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(userResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(userResponseDto1.getId()), Long.class));
    }

    @Test
    void testFindUserById() throws Exception {
        when(userService.findById(anyLong())).thenReturn(userResponseDto);

        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())))
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())));
    }

    @Test
    void testDeleteUser() throws Exception {
        mvc.perform(delete("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}