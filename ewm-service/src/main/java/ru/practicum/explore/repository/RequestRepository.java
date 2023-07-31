package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.explore.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("select r from ParticipationRequest r " +
            "where r.requester = :userId " +
            "and r.event.initiator.id <> :userId")
    List<ParticipationRequest> findByUserId(Long userId);

    @Query("select r from ParticipationRequest r " +
            "where r.event.initiator.id = :userId")
    List<ParticipationRequest> findByEventInitiatorId(Long userId);

    @Query("select r from ParticipationRequest r " +
            "where r.event.id = :eventId")
    List<ParticipationRequest> findByEventId(Long eventId);

    @Query("select r from ParticipationRequest r " +
            "where r.event.id in :eventIds")
    List<ParticipationRequest> findByEventIds(List<Long> eventIds);
}
