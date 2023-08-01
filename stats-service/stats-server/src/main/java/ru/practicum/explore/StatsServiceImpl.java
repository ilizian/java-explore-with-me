package ru.practicum.explore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.DtoMapper;
import ru.practicum.explore.model.ViewStats;
import ru.practicum.explore.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Autowired
    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) throws ValidationException {
        validateEndpointHit(endpointHitDto);
        return DtoMapper.toEndpointHitDto(statsRepository.save(DtoMapper.dtoToEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique)
            throws ValidationException {
        List<ViewStats> stats;
        if (start == null || end == null) {
            throw new ValidationException("Ошибка. Неправильный диапазон дат");
        }
        if (start.isAfter(end)) {
            throw new ValidationException("Ошибка. Дата начала диапазона не может быть больше даты окончания");
        }
        if (unique) {
            stats = statsRepository.findUniqueStats(start, end, uris);
        } else {
            stats = statsRepository.findStats(start, end, uris);
        }
        return getStatsListDto(stats);
    }

    private void validateEndpointHit(EndpointHitDto endpointHitDto) throws ValidationException {
        if (endpointHitDto.getUri() == null || endpointHitDto.getUri().isEmpty()) {
            throw new ValidationException("Ошибка. Поле uri не может быть пустым");
        }
        if (endpointHitDto.getApp() == null || endpointHitDto.getApp().isEmpty()) {
            throw new ValidationException("Ошибка. Поле app не может быть пустым");
        }
    }

    private List<ViewStatsDto> getStatsListDto(List<ViewStats> stats) {
        return stats.stream()
                .map(DtoMapper::toViewStatsDto)
                .collect(Collectors.toList());
    }
}