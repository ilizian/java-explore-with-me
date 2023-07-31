package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CompilationDto {
    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}