package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(Long eventId, Pageable page);

    List<Comment> findAllByAuthorId(Long userId, Pageable page);

    List<Comment> findAllByEventIdAndMessageContainsIgnoreCase(Long eventId, String message, Pageable page);

}