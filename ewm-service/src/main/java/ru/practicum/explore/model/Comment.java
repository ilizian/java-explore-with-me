package ru.practicum.explore.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String message;
    @ManyToOne
    private Event event;
    @ManyToOne
    private User author;
    @Column(nullable = false)
    private LocalDateTime created;
    private LocalDateTime updated;
}