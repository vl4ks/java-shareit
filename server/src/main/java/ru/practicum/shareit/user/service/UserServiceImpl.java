package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static ru.practicum.shareit.user.service.UserMapper.toUser;
import static ru.practicum.shareit.user.service.UserMapper.toUserDto;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return repository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    @Transactional
    public UserDto createUser(UserSaveDto userSaveDto) {
        log.info("Создание нового пользователя: {}", userSaveDto);
        User user = toUser(userSaveDto);
        User savedUser = repository.save(user);
        return toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по id = {}", id);
        User user = validateUserExists(id);
        return toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserSaveDto userSaveDto) {
        log.info("Обновление пользователя {} на {}", id, userSaveDto);
        User existingUser = validateUserExists(id);

        if (userSaveDto.getName() != null) {
            existingUser.setName(userSaveDto.getName());
        }
        if (userSaveDto.getEmail() != null) {
            existingUser.setEmail(userSaveDto.getEmail());
        }
        return toUserDto(repository.save(existingUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id = {} ", id);
        validateUserExists(id);
        repository.deleteById(id);
        log.info("Пользователь с id = {} успешно удалён", id);
    }


    private User validateUserExists(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
