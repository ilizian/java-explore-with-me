package ru.practicum.explore.service;

import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto compilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto);

    void deleteCompilationById(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
