package ru.practicum.explore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.service.CategoryService;
import ru.practicum.explore.service.CommentService;
import ru.practicum.explore.service.CompilationService;
import ru.practicum.explore.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
public class PublicController {

    private final EventService eventService;
    private final CompilationService compilationService;
    private final CategoryService categoryService;
    private final CommentService commentService;

    @Autowired
    public PublicController(EventService eventService, CompilationService compilationService,
                            CategoryService categoryService, CommentService commentService) {
        this.eventService = eventService;
        this.compilationService = compilationService;
        this.categoryService = categoryService;
        this.commentService = commentService;
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET. Получить список категорий");
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("GET. Получить категорию с id " + catId);
        return eventService.getCategoryById(catId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET. Получить список событий");
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("GET. Получить подборку с id " + compId);
        return compilationService.getCompilationById(compId);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEventsWithFilters(@RequestParam(required = false) String text,
                                                    @RequestParam(required = false) List<Long> categories,
                                                    @RequestParam(required = false) Boolean paid,
                                                    @RequestParam(required = false) String rangeStart,
                                                    @RequestParam(required = false) String rangeEnd,
                                                    @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                    @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(defaultValue = "10") Integer size,
                                                    HttpServletRequest request) throws ValidationException {
        log.info("GET. Получить список событий по заданным параметрам");
        return eventService.getEventsWithFilters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request.getRequestURI(), request.getRemoteAddr());
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET. Получить событие по id " + id);
        return eventService.getEventDtoById(id, request);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("GET. Получить комментарий по id " + commentId);
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getCommentsByText(@PathVariable Long eventId,
                                               @RequestParam(required = false) String message,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET. Получить комментарии к событию по id " + eventId);
        if (message == null) {
            return commentService.getCommentsOfEvent(eventId, from, size);
        }
        return commentService.getCommentsByText(eventId, message, from, size);
    }
}