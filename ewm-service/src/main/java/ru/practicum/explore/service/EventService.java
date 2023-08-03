package ru.practicum.explore.service;

import ru.practicum.explore.dto.*;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) throws ValidationException;

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEvent) throws ValidationException;

    void saveLocation(Event event);

    EventFullDto getEventOfUserByIds(Long userId, Long eventId) throws ValidationException;

    EventFullDto updateEventOfUserByIds(Long userId, Long eventId, UpdateEventUserRequest request) throws ValidationException;

    EventFullDto getEventDtoById(Long eventId, HttpServletRequest request);

    Event getEventById(Long eventId);

    Event updateEventWithUserRequest(Event event, UpdateEventUserRequest updateRequest) throws ValidationException;

    Event updateEventWithAdminRequest(Event event, UpdateEventAdminRequest updateRequest) throws ValidationException;


    CategoryDto getCategoryById(Long catId);

    List<EventShortDto> getEventsWithFilters(String text,
                                             List<Long> categories,
                                             Boolean paid,
                                             String rangeStart,
                                             String rangeEnd,
                                             Boolean onlyAvailable,
                                             String sort,
                                             Integer from,
                                             Integer size,
                                             String requestUri,
                                             String requestAddr) throws ValidationException;

    List<EventShortDto> createShortEventDtos(List<Event> events);

    EventFullDto getViewsCounter(EventFullDto eventFullDto);

    List<EventFullDto> getViewCounters(List<EventFullDto> dtos);
}
