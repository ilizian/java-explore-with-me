package ru.practicum.explore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explore.EndpointHitDto;
import ru.practicum.explore.ViewStatsClient;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.dtoMapper.CategoryDtoMapper;
import ru.practicum.explore.dtoMapper.EventDtoMapper;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.misc.EventState;
import ru.practicum.explore.model.*;
import ru.practicum.explore.repository.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final CategoryRepository categoryRepository;
    private final EventDtoMapper eventDtoMapper;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final ViewStatsClient viewStatsClient;
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEvent) throws ValidationException {
        User user = userRepository.getUserById(userId);
        Category category = categoryRepository.findById(newEvent.getCategory()).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория " + newEvent.getCategory())
        );
        Event event = eventDtoMapper.mapNewEventDtoToEvent(newEvent, category);
        saveLocation(event);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new ValidationException("Ошибка. Изменение невозможно, т.к. до начала событий меньше 2 часов");
        }
        event = eventRepository.save(event);
        return eventDtoMapper.mapEventToFullDto(event);
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime rangeStartDateTime;
        LocalDateTime rangeEndDateTime;
        if (rangeStart != null) {
            rangeStartDateTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        } else {
            rangeStartDateTime = LocalDateTime.now().minusYears(1000L);
        }
        if (rangeEnd != null) {
            rangeEndDateTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        } else {
            rangeEndDateTime = LocalDateTime.now().plusYears(1000L);
        }
        List<EventState> eventStateList;
        if (states != null) {
            eventStateList = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        } else {
            eventStateList = Arrays.stream(EventState.values()).collect(Collectors.toList());
        }
        List<Event> events = eventRepository.findAllEventsWithDates(users, eventStateList, categories, rangeStartDateTime,
                rangeEndDateTime, PageRequest.of(from / size, size));
        HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();
        for (Event event : events) {
            eventIdsWithViewsCounter.put(event.getId(), getViewsCounter(eventDtoMapper.mapEventToFullDto(event)).getViews());
        }
        List<ParticipationRequest> requests = requestRepository
                .findAllByEventIdInAndStatus(new ArrayList<>(eventIdsWithViewsCounter.keySet()), "CONFIRMED");
        List<EventFullDto> eventFullDtos = listEventToEventFullDto(events);
        for (EventFullDto eventFullDto : eventFullDtos) {
            for (ParticipationRequest request : requests) {
                if (request.getEvent().getId().equals(eventFullDto.getId())) {
                    eventFullDto.setConfirmedRequests(eventFullDto.getConfirmedRequests() + 1);
                }
            }
        }
        eventFullDtos = getViewCounters(eventFullDtos);
        return eventFullDtos;
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) throws ValidationException {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие с id " + eventId));
        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new ValidationException("Ошибка. Изменение невозможно, т.к. до начала событий меньше 2 часов");
        }
        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Ошибка. Неправильный статус");
        }
        updateEventWithAdminRequest(event, updateRequest);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Ошибка. Событие уже прошло");
        }
        saveLocation(event);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        eventFullDto.setConfirmedRequests(requestsCount);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        User user = userRepository.getUserById(userId);
        List<Event> events = eventRepository.findAllByInitiator(user, PageRequest.of(from / size, size));
        return listEventToEventShortDto(events);
    }

    @Override
    public void saveLocation(Event event) {
        event.setLocation(locationRepository.save(event.getLocation()));
    }

    @Override
    public EventFullDto getEventOfUserByIds(Long userId, Long eventId) throws ValidationException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Ошибка. Пользователь не является инициатором события с id " + eventId);
        }
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        eventFullDto.setConfirmedRequests(requestsCount);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public EventFullDto updateEventOfUserByIds(Long userId, Long eventId, UpdateEventUserRequest request) throws ValidationException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Ошибка. Пользователь не является инициатором события с id " + eventId);
        }
        event = updateEventWithUserRequest(event, request);
        saveLocation(event);
        eventRepository.save(event);
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        eventFullDto.setConfirmedRequests(requestsCount);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public EventFullDto getEventDtoById(Long eventId, HttpServletRequest request) {
        viewStatsClient.addHit(new EndpointHitDto("ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))));
        Event event = getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Ошибка. Событие не опубликовано");
        }
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        eventFullDto.setConfirmedRequests(requestsCount);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие с id " + eventId));
    }

    @Override
    public Event updateEventWithUserRequest(Event event, UpdateEventUserRequest updateRequest) throws ValidationException {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Ошибка. Не найдена категория " + updateRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateRequest.getEventDate(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    break;
                case "REJECT_EVENT":
                    event.setState(EventState.CANCELED);
                    break;
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Ошибка. Неправильный статус для события");
            }
        }
        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new ValidationException("Ошибка. Изменение невозможно, т.к. до начала событий меньше 2 часов");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Ошибка. Неправильный статус запроса");
        }
        return event;
    }

    @Override
    public Event updateEventWithAdminRequest(Event event, UpdateEventAdminRequest updateRequest) throws ValidationException {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Ошибка. Не найдена категория " + updateRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateRequest.getEventDate(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Ошибка. Неправильный статус для события");
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        return event;
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория с id " + catId));
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public List<EventShortDto> getEventsWithFilters(String text,
                                                    List<Long> categories,
                                                    Boolean paid,
                                                    String rangeStart,
                                                    String rangeEnd,
                                                    Boolean onlyAvailable,
                                                    String sort,
                                                    Integer from,
                                                    Integer size,
                                                    String requestUri,
                                                    String requestAddr) throws ValidationException {
        List<Event> events;
        LocalDateTime startDate;
        LocalDateTime endDate;
        if (sort.equals("EVENT_DATE")) {
            sort = "eventDate";
        } else {
            sort = "views";
        }
        if (rangeStart == null) {
            startDate = LocalDateTime.now();
        } else {
            startDate = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        }
        if (text == null) {
            text = "";
        }
        if (rangeEnd == null) {
            events = eventRepository.findEventsByText(text.toLowerCase(), PageRequest.of(from / size, size,
                    Sort.by(Sort.Direction.ASC, sort)));
        } else {
            endDate = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            if (endDate.isBefore(startDate) || endDate.equals(startDate)) {
                throw new ValidationException("Ошибка. Дата окончания диапазона не должны быть меньше даты начала");
            }
            events = eventRepository.findAllByTextAndDateRange(text.toLowerCase(),
                    startDate,
                    endDate,
                    PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, sort)));
        }
        viewStatsClient.addHit(new EndpointHitDto("ewm-service",
                requestUri,
                requestAddr,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))));
        return createShortEventDtos(events);
    }

    @Override
    public List<EventShortDto> createShortEventDtos(List<Event> events) {
        HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();
        for (Event event : events) {
            eventIdsWithViewsCounter.put(event.getId(), getViewsCounter(eventDtoMapper.mapEventToFullDto(event)).getViews());
        }
        List<ParticipationRequest> requests = requestRepository
                .findAllByEventIdInAndStatus(new ArrayList<>(eventIdsWithViewsCounter.keySet()), "CONFIRMED");
        List<EventShortDto> dtos = events.stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList());
        for (EventShortDto dto : dtos) {
            for (ParticipationRequest request : requests) {
                if (request.getEvent().getId().equals(dto.getId())) {
                    dto.setConfirmedRequests(dto.getConfirmedRequests() + 1);
                }
            }
            dto.setViews(eventIdsWithViewsCounter.get(dto.getId()));
        }
        return dtos;
    }

    @Override
    public EventFullDto getViewsCounter(EventFullDto eventFullDto) {
        Integer views = viewStatsClient.getStats(eventFullDto.getCreatedOn(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                List.of("/events/" + eventFullDto.getId()), true).size();
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getViewCounters(List<EventFullDto> dtos) {
        for (EventFullDto dto : dtos) {
            getViewsCounter(dto);
        }
        return dtos;
    }

    private List<EventFullDto> listEventToEventFullDto(List<Event> events) {
        return events.stream()
                .map(eventDtoMapper::mapEventToFullDto)
                .collect(Collectors.toList());
    }

    private List<EventShortDto> listEventToEventShortDto(List<Event> events) {
        return events.stream()
                .map(eventDtoMapper::mapEventToShortDto)
                .collect(Collectors.toList());
    }
}