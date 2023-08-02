package ru.practicum.explore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explore.EndpointHitDto;
import ru.practicum.explore.ViewStatsClient;
import ru.practicum.explore.dto.*;
import ru.practicum.explore.dtoMapper.CategoryDtoMapper;
import ru.practicum.explore.dtoMapper.EventDtoMapper;
import ru.practicum.explore.dtoMapper.RequestDtoMapper;
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
    private final RequestDtoMapper requestDtoMapper;
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
        return getViewsCounter(eventFullDto);
    }

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Ошибка. Название категории уже существует " + newCategoryDto.getName());
        }
        Category category = categoryRepository.save(categoryDtoMapper.mapNewDtoToCategory(newCategoryDto));
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public void deleteCategoryById(Long catId) {
        if (categoryIsEmpty(catId)) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ConflictException("Ошибка. Невозможно удалить категорию с id " + catId + " (не пустая)");
        }
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws ValidationException {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория по id " + catId)
        );
        if (categoryDto.getName() == null || categoryDto.getName().isBlank()) {
            throw new ValidationException("Ошибка. Имя категории не может быть пустым");
        }
        if (Objects.equals(category.getName(), categoryDto.getName())) {
            return categoryDtoMapper.mapCategoryToDto(category);
        }
        List<Category> categories = categoryRepository.findByName(categoryDto.getName());
        if (!categories.isEmpty()) {
            if (!Objects.equals(categories.get(0).getId(), category.getId())) {
                throw new ConflictException("Ошибка. Название категории уже существует " + categoryDto.getName());
            }
        }
        category.setName(categoryDto.getName());
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public boolean categoryIsEmpty(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория " + catId)
        );
        List<Event> eventsOfCategory = eventRepository.findAllByCategoryId(catId);
        return eventsOfCategory.isEmpty();
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Page<Category> categoryList = categoryRepository.findAll(PageRequest.of(from / size, size));
        return listCategoryToCategoryDto(categoryList);
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
        return getViewsCounter(eventFullDto);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId) throws ValidationException {
        List<ParticipationRequest> participationRequests = getParticipationRequests(userId, eventId);
        return listParticipationRequestToDto(participationRequests);
    }

    @Override
    public EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                                     Long eventId,
                                                                     EventRequestStatusUpdateRequest updateRequest) throws ValidationException {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        Event event = getEventById(eventId);
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        if (participationLimitIsFullCount(event, requestsCount)) {
            throw new ConflictException("Ошибка. Достигнут лимит заявок");
        }
        for (ParticipationRequest request : requests) {
            if (updateRequest.getRequestIds().contains(request.getId())) {
                request.setStatus(updateRequest.getStatus());
            }
        }
        List<ParticipationRequest> participationRequestList = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals("PENDING") || request.getStatus().equals("CONFIRMED") || request.getStatus().equals("REJECTED")) {
                result.addRequest(request);
                participationRequestList.add(request);
            } else {
                throw new ValidationException("Ошибка. Неправильный статус запроса");
            }
        }
        if (!participationRequestList.isEmpty()) {
            requestRepository.saveAll(participationRequestList);
        }
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId) {
        User user = userRepository.getUserById(userId);
        List<ParticipationRequest> participationRequests = requestRepository.findAllByRequester(user);
        return listParticipationRequestToDto(participationRequests);
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
        return getViewsCounter(eventFullDto);
    }

    @Override
    public List<ParticipationRequest> getParticipationRequests(Long userId, Long eventId) throws ValidationException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Ошибка. Пользователь не является инициатором события с id " + eventId);
        }
        return requestRepository.findAllByEventInitiatorId(userId);
    }

    @Override
    public List<ParticipationRequest> getParticipationRequestsByEventId(Long eventId) {
        Event event = getEventById(eventId);
        return requestRepository.findAllByEventId(eventId);
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
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Ошибка. Невозможно добавить заявку на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Ошибка. Событие не опубликовано");
        }
        Long requestsCount = requestRepository.countParticipationByEventIdAndStatus(event.getId(), "CONFIRMED");
        if (participationLimitIsFullCount(event, requestsCount)) {
            throw new ConflictException("Ошибка. Достигнут лимит заявок");
        }
        if (requestRepository.existsByRequesterId(userId)) {
            throw new ConflictException("Ошибка. Невозможно оставить заявку повторно");
        }
        ParticipationRequest newRequest = ParticipationRequest.builder()
                .requester(user)
                .created(LocalDateTime.now())
                .status("PENDING")
                .event(event)
                .build();
        if (event.getRequestModeration().equals(false) || event.getParticipantLimit() == 0) {
            newRequest.setStatus("CONFIRMED");
        }
        return requestDtoMapper.mapRequestToDto(requestRepository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        User user = userRepository.getUserById(userId);
        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найден запрос по id " + requestId)
        );
        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Ошибка. Заявка оставлена другим пользователем");
        }
        request.setStatus("CANCELED");
        return requestDtoMapper.mapRequestToDto(requestRepository.save(request));
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

    private List<CategoryDto> listCategoryToCategoryDto(Page<Category> categories) {
        return categories.stream()
                .map(categoryDtoMapper::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    private List<ParticipationRequestDto> listParticipationRequestToDto(List<ParticipationRequest> participationRequests) {
        return participationRequests.stream()
                .map(requestDtoMapper::mapRequestToDto)
                .collect(Collectors.toList());
    }

    private boolean participationLimitIsFullCount(Event event, Long requests) {
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= requests) {
            throw new ConflictException("Ошибка. Превышен предел количества заявок");
        }
        return false;
    }
}