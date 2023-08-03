package ru.practicum.explore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.CategoryDto;
import ru.practicum.explore.dto.NewCategoryDto;
import ru.practicum.explore.dtoMapper.CategoryDtoMapper;
import ru.practicum.explore.exception.ConflictException;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.model.Category;
import ru.practicum.explore.model.Event;
import ru.practicum.explore.repository.CategoryRepository;
import ru.practicum.explore.repository.EventRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Ошибка. Название категории уже существует " + newCategoryDto.getName());
        }
        Category category = categoryRepository.save(categoryDtoMapper.mapNewDtoToCategory(newCategoryDto));
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public void deleteCategoryById(Long catId) {
        if (categoryIsEmpty(catId)) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ConflictException("Ошибка. Невозможно удалить категорию с id " + catId + " (не пустая)");
        }
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) throws ValidationException {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория по id " + catId)
        );
        if (categoryDto.getName() == null || categoryDto.getName().isBlank()) {
            throw new ValidationException("Ошибка. Имя категории не может быть пустым");
        }
        if (Objects.equals(category.getName(), categoryDto.getName())) {
            return categoryDtoMapper.mapCategoryToDto(category);
        }
        List<Category> categories = categoryRepository.findByName(categoryDto.getName());
        if (!categories.isEmpty()) {
            if (!Objects.equals(categories.get(0).getId(), category.getId())) {
                throw new ConflictException("Ошибка. Название категории уже существует " + categoryDto.getName());
            }
        }
        category.setName(categoryDto.getName());
        return categoryDtoMapper.mapCategoryToDto(category);
    }

    @Override
    public boolean categoryIsEmpty(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Ошибка. Не найдена категория " + catId)
        );
        List<Event> eventsOfCategory = eventRepository.findAllByCategoryId(catId);
        return eventsOfCategory.isEmpty();
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Page<Category> categoryList = categoryRepository.findAll(PageRequest.of(from / size, size));
        return listCategoryToCategoryDto(categoryList);
    }

    private List<CategoryDto> listCategoryToCategoryDto(Page<Category> categories) {
        return categories.stream()
                .map(categoryDtoMapper::mapCategoryToDto)
                .collect(Collectors.toList());
    }
}
