package ru.practicum.explore.service;

import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.dto.NewCategoryDto;
import ru.practicum.explore.exception.ValidationException;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategoryById(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws ValidationException;

    boolean categoryIsEmpty(Long catId);

    List<CategoryDto> getCategories(Integer from, Integer size);
}
