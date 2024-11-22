package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.getAllUsers().stream().map(userMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto user) {
        return userMapper.toUserDto(repository.createUser(user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(repository.getUserById(id));
    }

    @Override
    public UserDto updateUser(Long id, UserDto user) {
        user.setId(id);
        return userMapper.toUserDto(repository.updateUser(userMapper.toUser(user)));
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteUser(id);
    }
}
