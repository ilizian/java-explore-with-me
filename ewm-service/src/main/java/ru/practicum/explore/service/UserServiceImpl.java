package ru.practicum.explore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dtoMapper.UserDtoMapper;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.User;
import ru.practicum.explore.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserDtoMapper userDtoMapper) {
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users = userRepository.findAllByIdsPageable(ids, PageRequest.of(from / size, size));
        return users.stream()
                .map(userDtoMapper::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найден пользователь с id " + userId));
    }

    @Override
    public UserDto addUser(NewUserRequest newUserDto) {
        if (userRepository.findByName(newUserDto.getName()).size() > 0) {
            throw new ConflictException("Ошибка. Занято имя пользователя " + newUserDto.getName());
        }
        User savedUser = userRepository.save(userDtoMapper.mapNewUserRequestToUser(newUserDto));
        return userDtoMapper.mapUserToDto(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
