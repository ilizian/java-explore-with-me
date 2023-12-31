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
    @Column(name = "app", nullable = false)
    private String app;
    @NotNull
    @Column(name = "uri", nullable = false)
    private String uri;
    @NotNull
    @Column(name = "ip", nullable = false)
    private String ip;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "timestamp", nullable = false)
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
        return id.equals(thisHit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}