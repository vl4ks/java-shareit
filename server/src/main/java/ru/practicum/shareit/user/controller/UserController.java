package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.debug("Получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.debug("Получение пользователя по id =  {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@RequestBody @Validated(ValidationGroups.Create.class) UserSaveDto user) {
        log.debug("Создание нового пользователя: {}", user);
        return userService.createUser(user);
    }


    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody @Validated(ValidationGroups.Update.class) UserSaveDto userSaveDto) {
        log.debug("Обновление пользователя {}", userSaveDto);
        return userService.updateUser(userId, userSaveDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.debug("Удаление пользователя с id = {}: ", id);
        userService.deleteUser(id);
    }
}