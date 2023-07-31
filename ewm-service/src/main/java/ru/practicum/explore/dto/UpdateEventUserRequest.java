package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.explore.model.Location;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UpdateEventUserRequest {
    @Size(max = 2000, min = 20)
    private String annotation;
    private Long category;
    @Size(max = 7000, min = 20)
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    public String stateAction;
    @Size(max = 120, min = 3)
    private String title;
}