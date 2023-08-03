package ru.practicum.explore.service;

import ru.practicum.explore.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.ParticipationRequest;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) throws ValidationException;

    List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId);

    List<ParticipationRequest> getParticipationRequests(Long userId, Long eventId) throws ValidationException;

    List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId) throws ValidationException;

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);
}
