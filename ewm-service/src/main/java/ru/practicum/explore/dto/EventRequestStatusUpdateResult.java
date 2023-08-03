package ru.practicum.explore.dto;

import lombok.*;

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
}
