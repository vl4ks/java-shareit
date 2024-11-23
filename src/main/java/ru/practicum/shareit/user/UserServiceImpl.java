package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return repository.getAllUsers().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto);
        User user = UserMapper.toUser(userDto);
        User createdUser = repository.createUser(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по id = {}", id);
        return UserMapper.toUserDto(repository.getUserById(id));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя {} на {}", id, userDto);
        User user = UserMapper.toUser(userDto);
        user.setId(id);
        User updatedUser = repository.updateUser(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id = {} ", id);
        repository.deleteUser(id);
    }
}
