package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ParticipationRequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private String status;
}