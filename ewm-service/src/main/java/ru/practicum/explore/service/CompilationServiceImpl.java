package ru.practicum.explore.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CompilationDto;
import ru.practicum.explore.dto.NewCompilationDto;
import ru.practicum.explore.dto.UpdateCompilationRequest;
import ru.practicum.explore.dtoMapper.CompilationDtoMapper;
import ru.practicum.explore.dtoMapper.EventDtoMapper;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.Compilation;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.repository.CompilationRepository;
import ru.practicum.explore.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationDtoMapper compilationDtoMapper;
    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        List<Event> events = eventRepository.findEventsByIds(compilationDto.getEvents());
        Compilation compilation = compilationDtoMapper.mapNewCompilationDtoToCompilation(compilationDto, events);
        compilation = compilationRepository.save(compilation);
        log.info("Подборка сохранена с id=" + compilation.getId());
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена"));
        List<Event> events = eventRepository.findEventsByIds(compilationDto.getEvents());
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(events);
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        compilation = compilationRepository.save(compilation);
        log.info("Подборка id=" + compId + " обновлена");
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена"));
        compilationRepository.deleteById(compId);
        log.info("Подборка id=" + compId + " удалена");
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        List<CompilationDto> compilationDtos = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationDto dto = compilationDtoMapper.mapCompilationToDto(compilation);
            dto.setEvents(compilation.getEvents().stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList()));
            compilationDtos.add(dto);
        }
        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Ошибка. Подборка id=" + compId + " не найдена")
        );
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream().map(eventDtoMapper::mapEventToShortDto).collect(Collectors.toList()));
        log.info("Подборка id=" + compId + " найдена");
        return result;
    }
}
