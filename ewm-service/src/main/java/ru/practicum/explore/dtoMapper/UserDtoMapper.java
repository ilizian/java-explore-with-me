package ru.practicum.explore.dtoMapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dto.UserShortDto;
import ru.practicum.explore.model.User;

@Component
public class UserDtoMapper {
    public UserDto mapUserToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public UserShortDto mapUserToShortDto(User user) {
        if (user.getId() == null) {
            return UserShortDto.builder()
                    .id(null)
                    .name(user.getName())
                    .build();
        }
        return new UserShortDto(user.getId(), user.getName());
    }

    public User mapNewUserRequestToUser(NewUserRequest newUser) {
        return new User(
                null,
                newUser.getEmail(),
                newUser.getName()
        );
    }
}
