package ru.practicum.explore.service;

import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    User getUserById(Long userId);

    UserDto addUser(NewUserRequest newUserDto);

    void deleteUser(Long userId);
}
