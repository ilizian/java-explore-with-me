package ru.practicum.explore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ViewStatsClient extends BaseClient {

    @Autowired
    public ViewStatsClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> getStatsSpecified(String start, String end, String[] uris, Boolean unique) {
        Map<String, Object> params = Map.of(
                "start", start,
                "end", end,
                "uris", String.join(",",uris),
                "unique", unique);
        return get("?start={start}&end={end}&uris={uris}&unique={unique}", params);
    }

    public ResponseEntity<Object> getStats(String start, String end, Boolean unique) {
        Map<String, Object> params = Map.of(
                "start", start,
                "end", end,
                "unique", unique);
        return get("?start={start}&end={end}&unique={unique}", params);
    }
}