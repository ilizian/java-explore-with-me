package ru.practicum.explore.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CommentDto;
import ru.practicum.explore.dtoMapper.CommentDtoMapper;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.Comment;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.User;
import ru.practicum.explore.repository.CommentRepository;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentDtoMapper commentDtoMapper;


    @Override
    public CommentDto addNewComment(Long userId, Long eventId, CommentDto newCommentDto) throws ValidationException {
        if (newCommentDto.getMessage() == null || newCommentDto.getMessage().isBlank()) {
            throw new ValidationException("Ошибка. Отсутствует текст комментария");
        }
        Comment comment = commentRepository.save(getCommentFromDto(userId, eventId, newCommentDto));
        return commentDtoMapper.mapCommentToDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsOfEvent(Long eventId, Integer from, Integer size) {
        List<Comment> comments = commentRepository.findAllByEventId(eventId, PageRequest.of(from / size, size));
        return comments.stream()
                .map(commentDtoMapper::mapCommentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsOfUser(Long userId, Integer from, Integer size) {
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, PageRequest.of(from / size, size));
        return comments.stream()
                .map(commentDtoMapper::mapCommentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentDto updateCommentDto) {
        Comment newComment = getCommentFromDto(userId, commentId, updateCommentDto);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найден комментарий по id " + commentId)
        );
        comment.setMessage(newComment.getMessage());
        comment.setUpdated(LocalDateTime.now());
        commentRepository.save(comment);
        return commentDtoMapper.mapCommentToDto(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Ошибка. Не найден комментарй по id " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByText(Long eventId, String text, Integer from, Integer size) {
        return commentRepository.findAllByEventIdAndText(eventId, text.toLowerCase(), PageRequest.of(from / size, size));
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найден комментарй по id " + commentId));
        return commentDtoMapper.mapCommentToDto(comment);
    }

    private Comment getCommentFromDto(Long userId, Long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найден пользователь по id " + userId)
        );
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие по id " + eventId)
        );
        return Comment.builder()
                .message(commentDto.getMessage())
                .author(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();
    }
}