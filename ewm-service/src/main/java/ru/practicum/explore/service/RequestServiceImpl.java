package ru.practicum.explore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.dtoMapper.RequestDtoMapper;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.misc.EventState;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.model.ParticipationRequest;
import ru.practicum.explore.model.User;
import ru.practicum.explore.repository.EventRepository;
import ru.practicum.explore.repository.RequestRepository;
import ru.practicum.explore.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestDtoMapper requestDtoMapper;

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.getUserById(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие с id " + eventId));
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
    public EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                                     Long eventId,
                                                                     EventRequestStatusUpdateRequest updateRequest) throws ValidationException {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие с id " + eventId));
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
    public List<ParticipationRequest> getParticipationRequests(Long userId, Long eventId) throws ValidationException {
        User user = userRepository.getUserById(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдено событие с id " + eventId));
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Ошибка. Пользователь не является инициатором события с id " + eventId);
        }
        return requestRepository.findAllByEventInitiatorId(userId);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId) throws ValidationException {
        List<ParticipationRequest> participationRequests = getParticipationRequests(userId, eventId);
        return listParticipationRequestToDto(participationRequests);
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
