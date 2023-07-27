package ru.practicum.explore.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hits")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(nullable = false)
    private String app;
    @NotNull
    @Column(nullable = false)
    private String uri;
    @NotNull
    @Column(nullable = false)
    private String ip;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "datetime", nullable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        EndpointHit thisHit = (EndpointHit) other;
        return id.equals(thisHit.id)
                && app.equals(thisHit.app)
                && uri.equals(thisHit.uri)
                && ip.equals(thisHit.ip)
                && timestamp.equals(thisHit.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, app, uri, ip, timestamp);
    }
}