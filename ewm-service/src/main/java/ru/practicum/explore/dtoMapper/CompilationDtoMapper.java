package ru.practicum.explore.dtoMapper;


import org.springframework.stereotype.Component;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompilationDtoMapper {
    private final EventDtoMapper eventDtoMapper;

    public CompilationDtoMapper(EventDtoMapper eventDtoMapper) {
        this.eventDtoMapper = eventDtoMapper;
    }

    public CompilationDto mapCompilationToDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(compilation.getEvents().stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList()))
                .build();
    }

    public Compilation mapNewCompilationDtoToCompilation(NewCompilationDto dto, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }
}