package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long id);
}
