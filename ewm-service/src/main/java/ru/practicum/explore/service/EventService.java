package ru.practicum.explore.service;

import ru.practicum.explore.dto.*;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.ParticipationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) throws ValidationException;

    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategoryById(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws ValidationException;

    boolean categoryIsEmpty(Long catId);

    List<CategoryDto> getCategories(Integer from, Integer size);

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEvent);

    void saveLocation(Event event);

    EventFullDto getEventOfUserByIds(Long userId, Long eventId) throws ValidationException;

    EventFullDto updateEventOfUserByIds(Long userId, Long eventId, UpdateEventUserRequest request) throws ValidationException;

    List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId) throws ValidationException;

    EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) throws ValidationException;

    List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId);

    EventFullDto getEventDtoById(Long eventId, HttpServletRequest request);

    List<ParticipationRequest> getParticipationRequests(Long userId, Long eventId) throws ValidationException;

    List<ParticipationRequest> getParticipationRequestsByEventId(Long eventId);

    Event getEventById(Long eventId);

    Event updateEventWithUserRequest(Event event, UpdateEventUserRequest updateRequest) throws ValidationException;

    Event updateEventWithAdminRequest(Event event, UpdateEventAdminRequest updateRequest) throws ValidationException;

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

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
                                             HttpServletRequest request) throws ValidationException;

    List<EventShortDto> createShortEventDtos(List<Event> events);

    EventFullDto getViewsCounter(EventFullDto eventFullDto);

    List<EventFullDto> getViewCounters(List<EventFullDto> dtos);
}
