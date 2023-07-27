package ru.practicum.explore.model;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewStats viewStats = (ViewStats) o;
        return hits.equals(viewStats.hits) && app.equals(viewStats.app) && uri.equals(viewStats.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, uri, hits);
    }
}