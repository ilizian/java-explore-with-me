package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.ParticipationRequest;
import ru.practicum.explore.model.User;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequester(User user);

    List<ParticipationRequest> findAllByEventInitiatorId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, String status);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    Long countParticipationByEventIdAndStatus(Long eventId, String status);

    Boolean existsByRequesterId(Long userId);
}
