package ru.practicum.explore.dtoMapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.dto.NewCategoryDto;
import ru.practicum.explore.model.Category;

@Component
public class CategoryDtoMapper {
    public Category mapNewDtoToCategory(NewCategoryDto categoryDto) {
        return Category.builder()
                .id(null)
                .name(categoryDto.getName())
                .build();
    }

    public CategoryDto mapCategoryToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
