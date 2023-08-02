package ru.practicum.explore.dtoMapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore.dto.ParticipationRequestDto;
import ru.practicum.explore.model.ParticipationRequest;

@Component
public class RequestDtoMapper {
    public ParticipationRequestDto mapRequestToDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .event(participationRequest.getEvent().getId())
                .status(participationRequest.getStatus())
                .created(participationRequest.getCreated().toString())
                .build();
    }
}