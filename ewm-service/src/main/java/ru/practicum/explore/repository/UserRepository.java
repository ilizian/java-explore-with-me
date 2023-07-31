package ru.practicum.explore.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> getAllByIdIsIn(List<Long> ids, Pageable page);

    List<User> findByName(String name);
}