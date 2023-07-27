package ru.practicum.explore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EndpointHitClient extends BaseClient {

    @Autowired
    public EndpointHitClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> addHit(EndpointHitDto hit) {
        return post("", hit);
    }
}