package ru.practicum.explore.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.service.CommentService;
import ru.practicum.explore.service.EventService;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.service.RequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@Slf4j
public class PrivateController {
    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    @Autowired
    public PrivateController(EventService eventService, RequestService requestService, CommentService commentService) {
        this.eventService = eventService;
        this.requestService = requestService;
        this.commentService = commentService;
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getRequestsOfUser(@PathVariable Long userId) {
        log.info("GET. Получить информацию о заявках пользователя c id " + userId);
        return requestService.getParticipationRequestsByUserId(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                           @RequestParam Long eventId) {
        log.info("POST. Создать запрос на участие пользователя с id " + userId);
        return requestService.addParticipationRequest(userId, eventId);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET. Получить события по пользователю с id " + userId);
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewEventByUser(@PathVariable Long userId,
                                          @RequestBody @Valid NewEventDto newEvent) throws ValidationException {
        log.info("POST. Создать событие по пользователю с id " + userId);
        return eventService.addEvent(userId, newEvent);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventOfUserByIds(@PathVariable Long userId,
                                            @PathVariable Long eventId) throws ValidationException {
        log.info("GET. Получить событие с id " + eventId);
        return eventService.getEventOfUserByIds(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEventOfUserByIds(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @RequestBody @Valid UpdateEventUserRequest request) throws ValidationException {
        log.info("PATCH. Изменить событие с id " + eventId);
        return eventService.updateEventOfUserByIds(userId, eventId, request);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequest(@PathVariable Long userId,
                                                                 @PathVariable Long eventId) throws ValidationException {
        log.info("GET. Получить запросы на участие в событии по пользователю c id " + userId);
        return requestService.getParticipationRequestsDto(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateParticipationRequest(@PathVariable Long userId,
                                                                     @PathVariable Long eventId,
                                                                     @RequestBody @Valid EventRequestStatusUpdateRequest request)
            throws ValidationException {
        log.info("PATCH. Измененить статус заявок на участие в событии по пользователю c id " + userId);
        return requestService.updateParticipationRequest(userId, eventId, request);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId,
                                                              @PathVariable Long requestId) {
        log.info("PATCH. Отменить запрос на участие  пользователем c id " + userId);
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getCommentsByUser(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET. Получить список комментариев пользователя с id " + userId);
        return commentService.getCommentsOfUser(userId, from, size);
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNewComment(@PathVariable Long userId,
                                    @RequestParam Long eventId,
                                    @RequestBody @Valid CommentDto newCommentDto) {
        log.info("POST. Создать комментарий к событию id " + eventId);
        return commentService.addNewComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid CommentDto updateCommentDto) throws ValidationException {
        log.info("PATCH. Изменить комментарий с id " + commentId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }
}