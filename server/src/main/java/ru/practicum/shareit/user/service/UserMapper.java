package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.model.User;

@RequiredArgsConstructor
public final class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public static User toUser(UserSaveDto userSaveDto) {
        return new User(null, userSaveDto.getName(), userSaveDto.getEmail());
    }
}
