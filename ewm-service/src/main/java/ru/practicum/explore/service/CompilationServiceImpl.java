package ru.practicum.explore.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.dto.UpdateCompilationRequest;
import ru.practicum.explore.dtoMapper.CompilationDtoMapper;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.repository.CompilationRepository;
import ru.practicum.explore.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationDtoMapper compilationDtoMapper;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        List<Event> events = new ArrayList<>();
        if (compilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilationDto.getEvents());
        }
        Compilation compilation = compilationDtoMapper.mapNewCompilationDtoToCompilation(compilationDto, events);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        compilation = compilationRepository.save(compilation);
        return compilationDtoMapper.mapCompilationToDto(compilation);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена"));
        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(compilationDto.getEvents());
            compilation.setEvents(events);
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        compilation = compilationRepository.save(compilation);
        return compilationDtoMapper.mapCompilationToDto(compilation);
    }

    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена"));
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations;
        if (pinned == null) {
            Page<Compilation> compilationsPage = compilationRepository.findAll(PageRequest.of(from / size, size));
            compilations = compilationsPage.toList();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        }
        List<CompilationDto> compilationDtos = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationDto dto = compilationDtoMapper.mapCompilationToDto(compilation);
            compilationDtos.add(dto);
        }
        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена")
        );
        return compilationDtoMapper.mapCompilationToDto(compilation);
    }
}
