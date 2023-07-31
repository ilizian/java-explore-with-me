package ru.practicum.explore.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String annotation;
    private Long confirmedRequests;
    private CategoryDto category;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Integer views;
}