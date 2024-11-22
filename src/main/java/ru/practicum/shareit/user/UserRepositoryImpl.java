package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Repository
@Component("userRepositoryImpl")
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> userStorage = new HashMap<>();
    private final UserMapper userMapper;
    private long idGenerator = 1;

    @Override
    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(userStorage.values());
    }

    @Override
    public User getUserById(Long id) {
        log.info("Получение пользователя по id=: {}", id);
        if (userStorage.containsKey(id)) {
            return userStorage.get(id);
        }
        throw new NotFoundException("Пользователь с id = " + id + " не найден");
    }

    @Override
    public User createUser(UserDto user) {
        log.info("Создание нового пользователя: {}", user);
        User newUser = userMapper.toUser(user);
        validateUserForCreation(newUser);

        boolean emailExists = userStorage.values().stream()
                .anyMatch(existingUser ->
                        existingUser.getEmail() != null
                                && existingUser.getEmail().equalsIgnoreCase(newUser.getEmail()));
        if (emailExists) {
            log.error("Ошибка при создании пользователя: email '{}' уже существует", newUser.getEmail());
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        newUser.setId(idGenerator++);
        userStorage.put(newUser.getId(), newUser);

        log.info("Пользователь с id = {} успешно создан", newUser.getId());
        return newUser;
    }


    @Override
    public User updateUser(User user) {
        validateUserExists(user.getId());
        validateEmailForUpdate(user);

        User userToUpdate = userStorage.get(user.getId());
        log.info("Обновление пользователя {} на {}", userToUpdate, user);

        boolean isUpdated = updateUserFields(userToUpdate, user);

        if (!isUpdated) {
            log.info("Не произошло никаких изменений для пользователя с id = {}", user.getId());
        } else {
            log.info("Пользователь успешно обновлён: {}", userToUpdate);
        }

        return userToUpdate;
    }


    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id= {}: ", id);
        if (!userStorage.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        userStorage.remove(id);
        log.info("Пользователь с id = {} успешно удален", id);
    }

    private void validateUserForCreation(User user) {
        log.info("Валидация пользователя для создания");
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка при создании пользователя: некорректный email");
            throw new ValidationException("Имейл должен быть указан и содержать символ '@'");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
    }

    private void validateEmailForUpdate(User user) {
        if (user.getEmail() != null) {
            boolean emailExists = userStorage.values().stream()
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                    .map(User::getEmail)
                    .anyMatch(email -> email.equals(user.getEmail()));
            if (emailExists) {
                throw new ConflictException("Пользователь с email " + user.getEmail() + " уже существует.");
            }
        }
    }

    private boolean updateUserFields(User userToUpdate, User newUser) {
        boolean isUpdated = false;

        if (newUser.getEmail() != null) {
            userToUpdate.setEmail(newUser.getEmail());
            isUpdated = true;
        }

        if (newUser.getName() != null) {
            userToUpdate.setName(newUser.getName());
            isUpdated = true;
        }

        return isUpdated;
    }
}