package ru.practicum.explore;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ViewStatsClient {
    private static final String URL = "http://stats-server:9090";
    private final WebClient webClient = WebClient.create(URL);

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        return List.of(webClient.get()
                .uri(uriWithParams -> uriWithParams.path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToMono(ViewStatsDto[].class)
                .block());
    }

    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        return webClient.post()
                .uri("/hit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(endpointHitDto), EndpointHitDto.class)
                .retrieve()
                .bodyToMono(EndpointHitDto.class)
                .block();
    }
}