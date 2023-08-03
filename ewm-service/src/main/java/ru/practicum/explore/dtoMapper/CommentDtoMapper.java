package ru.practicum.explore.dtoMapper;


import org.springframework.stereotype.Component;
import ru.practicum.explore.dto.CommentDto;
import ru.practicum.explore.model.Comment;

import java.time.format.DateTimeFormatter;

@Component
public class CommentDtoMapper {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    UserDtoMapper userDtoMapper = new UserDtoMapper();
    EventDtoMapper eventDtoMapper = new EventDtoMapper();

    public CommentDto mapCommentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .message(comment.getMessage())
                .event(eventDtoMapper.mapEventToShortDto(comment.getEvent()))
                .author(userDtoMapper.mapUserToShortDto(comment.getAuthor()))
                .created(comment.getCreated().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .updated(comment.getUpdated() != null ? comment
                        .getUpdated().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) : null)
                .build();
    }
}