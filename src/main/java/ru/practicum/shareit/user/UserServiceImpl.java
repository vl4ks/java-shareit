package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return repository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto);
        User user = toUser(userDto);

        validateUserForCreation(user);

        if (repository.existsByEmailIgnoreCase(user.getEmail())) {
            log.error("Ошибка при создании пользователя: email '{}' уже существует", user.getEmail());
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        User createdUser = repository.save(user);
        log.info("Пользователь с id = {} успешно создан", createdUser.getId());

        return toUserDto(createdUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по id = {}", id);
        User user = validateUserExists(id);
        return toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя {} на {}", id, userDto);
        User existingUser = validateUserExists(id);
        User newUser = toUser(userDto);
        validateEmailForUpdate(existingUser, newUser);

        boolean isUpdated = updateUserFields(existingUser, newUser);

        if (isUpdated) {
            User updatedUser = repository.save(existingUser);
            log.info("Пользователь с id = {} успешно обновлён", updatedUser.getId());
            return UserMapper.toUserDto(updatedUser);
        } else {
            log.info("Не произошло никаких изменений для пользователя с id = {}", existingUser.getId());
            return UserMapper.toUserDto(existingUser);
        }
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id = {} ", id);
        validateUserExists(id);
        repository.deleteById(id);
        log.info("Пользователь с id = {} успешно удалён", id);
    }

    private void validateUserForCreation(User user) {
        log.info("Валидация пользователя для создания");
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка при создании пользователя: некорректный email");
            throw new ValidationException("Имейл должен быть указан и содержать символ '@'");
        }
    }

    private User validateUserExists(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private void validateEmailForUpdate(User existingUser, User newUser) {
        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            boolean emailExists = repository.existsByEmailIgnoreCase(newUser.getEmail());
            if (emailExists) {
                throw new ConflictException("Пользователь с email '" + newUser.getEmail() + "' уже существует.");
            }
        }
    }

    private boolean updateUserFields(User existingUser, User newUser) {
        boolean isUpdated = false;

        if (newUser.getEmail() != null && !newUser.getEmail().equals(existingUser.getEmail())) {
            log.debug("Обновление email: '{}' -> '{}'", existingUser.getEmail(), newUser.getEmail());
            existingUser.setEmail(newUser.getEmail());
            isUpdated = true;
        }

        if (newUser.getName() != null && !newUser.getName().equals(existingUser.getName())) {
            log.debug("Обновление имени: '{}' -> '{}'", existingUser.getName(), newUser.getName());
            existingUser.setName(newUser.getName());
            isUpdated = true;
        }

        return isUpdated;
    }
}
