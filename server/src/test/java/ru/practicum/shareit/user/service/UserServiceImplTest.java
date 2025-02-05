package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.model.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    private final User user1 = new User(1L, "User1", "user1@email.com");
    private final UserDto userDto1 = new UserDto(1L, "User1", "user1@email.com");
    private final UserSaveDto userSaveDto1 = new UserSaveDto("User1", "user1@email.com");
    private final User user2 = new User(2L, "User2", "user2@email.com");
    private final UserDto userDto2 = new UserDto(2L, "User2", "user2@email.com");

    @Test
    void testGetAllUsers() {

        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(users, List.of(userDto1, userDto2));
    }

    @Test
    void testGetUserById() {

        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));

        var sut = userService.getUserById(1L);

        assertEquals(sut, userDto1);
    }

    @Test
    void testCreateUser() {

        Mockito.when(userRepository.save(any()))
                .thenReturn(user1);

        assertEquals(userService.createUser(userSaveDto1), userDto1);
    }

    @Test
    void testUpdateUser() {

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        User updateUser = new User(1L, "updateUser", "updateUser@email.com");
        UserDto updateUserAfterDto = new UserDto(1L, "updateUser", "updateUser@email.com");
        UserSaveDto updateUserDto = new UserSaveDto("updateUser", "updateUser@email.com");
        Mockito.when(userRepository.save(any()))
                .thenReturn(updateUser);

        assertEquals(userService.updateUser(1L, updateUserDto), updateUserAfterDto);
    }

    @Test
    void testUpdateUserName() {

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        User updateUser = new User(1L, null, "updateUser@email.com");
        UserDto updateUserAfterDto = new UserDto(1L, null, "updateUser@email.com");
        UserSaveDto updateUserDto = new UserSaveDto(null, "updateUser@email.com");
        Mockito.when(userRepository.save(any()))
                .thenReturn(updateUser);

        assertEquals(userService.updateUser(1L, updateUserDto), updateUserAfterDto);
    }

    @Test
    void testUpdateUserEmail() {

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        User updateUser = new User(1L, "updateUser", null);
        UserDto updateUserAfterDto = new UserDto(1L, "updateUser", null);
        UserSaveDto updateUserDto = new UserSaveDto("updateUser", null);
        Mockito.when(userRepository.save(any()))
                .thenReturn(updateUser);

        assertEquals(userService.updateUser(1L, updateUserDto), updateUserAfterDto);
    }

    @Test
    void testDeleteUser() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        userService.deleteUser(1L);

        Mockito.verify(userRepository).deleteById(1L);
    }
}
