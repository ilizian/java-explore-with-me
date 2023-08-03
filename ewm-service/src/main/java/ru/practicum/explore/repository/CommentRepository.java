package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.explore.dto.CommentDto;
import ru.practicum.explore.model.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(Long eventId, Pageable page);

    List<Comment> findAllByAuthorId(Long userId, Pageable page);

    @Query("select c from Comment c " +
            "where c.event.id = :eventId " +
            "and lower(event.description) like %:message% " +
            "order by c.created desc")
    List<CommentDto> findAllByEventIdAndText(Long eventId, String message, Pageable page);

}