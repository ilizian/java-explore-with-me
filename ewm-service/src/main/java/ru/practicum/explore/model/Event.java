package ru.practicum.explore.model;

import lombok.*;
import ru.practicum.explore.misc.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000)
    private String annotation;
    @ManyToOne(optional = false)
    @JoinTable(name = "events_to_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(length = 7000)
    private String description;
    @Column(nullable = false, name = "event_date")
    private LocalDateTime eventDate;
    @ManyToOne(optional = false)
    private User initiator;
    @ManyToOne(optional = false)
    private Location location;
    @Column(nullable = false)
    private Boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    private EventState state;
    @Column(nullable = false)
    private String title;
    @ManyToMany
    @JoinTable(name = "events_to_compilations",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Compilation> compilationList;
}