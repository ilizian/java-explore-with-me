package ru.practicum.explore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.exception.ValidationException;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Service
@RestController
public class StatsController {
    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(@RequestParam(value = "start") String start,
                                       @RequestParam(value = "end") String end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(required = false, defaultValue = "false") Boolean unique)
            throws ValidationException {
        log.info("GET. Получить статистику");
        return statsService.getStats(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addEndpointHit(@Valid @RequestBody EndpointHitDto endpointHitDto) throws ValidationException {
        log.info("POST. Сохранить запрос к эндпоинту");
        return statsService.saveHit(endpointHitDto);
    }
}