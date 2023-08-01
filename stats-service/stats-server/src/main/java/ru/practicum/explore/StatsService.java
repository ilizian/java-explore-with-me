package ru.practicum.explore;

import ru.practicum.explore.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto saveHit(EndpointHitDto endpointHitDto) throws ValidationException;

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) throws ValidationException;
}
