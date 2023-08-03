package ru.practicum.explore.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByName(String name);

    Boolean existsByName(String name);
}