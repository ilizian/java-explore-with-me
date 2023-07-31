package ru.practicum.explore.dto;

import lombok.*;
import ru.practicum.explore.model.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateResult {
    private final List<ParticipationRequestDto> confirmedRequests;
    private final List<ParticipationRequestDto> rejectedRequests;

    public EventRequestStatusUpdateResult() {
        confirmedRequests = new ArrayList<>();
        rejectedRequests = new ArrayList<>();
    }

    public void addRequest(ParticipationRequest request) {
        String created = request.getCreated().toString();
        Long event = request.getEvent().getId();
        Long id = request.getId();
        Long requester = request.getRequester();
        String status = request.getStatus();
        ParticipationRequestDto result = new ParticipationRequestDto(created, event, id, requester, status);
        if ("CONFIRMED".equals(status)) {
            confirmedRequests.add(result);
        } else if ("REJECTED".equals(status)) {
            rejectedRequests.add(result);
        }
    }
}
