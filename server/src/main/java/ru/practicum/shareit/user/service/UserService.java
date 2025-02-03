package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserSaveDto userSaveDto);

    UserDto updateUser(Long id, UserSaveDto userSaveDto);

    void deleteUser(Long id);

}
