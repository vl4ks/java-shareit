package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testCreateUserIntegration() {
        UserSaveDto userSaveDto = new UserSaveDto("User1", "user1@email.com");

        UserDto createdUser = userService.createUser(userSaveDto);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("User1", createdUser.getName());
        assertEquals("user1@email.com", createdUser.getEmail());

        Optional<User> savedUser = userRepository.findById(createdUser.getId());
        assertTrue(savedUser.isPresent());
        assertEquals("User1", savedUser.get().getName());
        assertEquals("user1@email.com", savedUser.get().getEmail());
    }
}
