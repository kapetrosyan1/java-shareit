package ru.practicum.shareit.user.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    public User toUser(UserRequestDto userRequestDto) {
        User user = new User();
        user.setId(userRequestDto.getId());
        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        return user;
    }

    public UserResponseDto toUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setName(user.getName());
        userResponseDto.setEmail(user.getEmail());
        return userResponseDto;
    }

    public User fromUserCreationDto(UserCreationDto userCreationDto) {
        User user = new User();
        user.setId(userCreationDto.getId());
        user.setName(userCreationDto.getName());
        user.setEmail(userCreationDto.getEmail());
        return user;
    }
}