package ru.practicum.explore.dto;

import lombok.*;
import ru.practicum.explore.model.Location;

import org.springframework.beans.factory.annotation.Value;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    @Value("true")
    private Boolean requestModeration;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}