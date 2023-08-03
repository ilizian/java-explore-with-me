package ru.practicum.explore.service;

import ru.practicum.explore.dto.CommentDto;
import ru.practicum.explore.exception.ValidationException;

import java.util.List;

public interface CommentService {

    List<CommentDto> getCommentsOfUser(Long userId, Integer from, Integer size);

    CommentDto addNewComment(Long userId, Long eventId, CommentDto newCommentDto) throws ValidationException;

    CommentDto updateComment(Long userId, Long commentId, CommentDto updateCommentDto);

    List<CommentDto> getCommentsOfEvent(Long eventId, Integer from, Integer size);

    List<CommentDto> getCommentsByText(Long eventId, String text, Integer from, Integer size);

    CommentDto getCommentById(Long commentId);

    void deleteComment(Long commentId);
}
