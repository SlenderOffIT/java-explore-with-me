package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.CompilationNotFoundException;
import ru.practicum.exceptions.ForbiddenArgumentException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategoryByAdmin(CategoryDto dto) {
        log.info("Обрабатываем запрос на создание категории {}", dto);
        return CategoryMapper.mapperToCategoryDto(categoryRepository.save(CategoryMapper.mapperToCategory(dto)));
    }

    @Override
    public CategoryDto updateCategoryByAdmin(long catId, CategoryDto dto) {
        log.info("Обрабатываем запрос на обновление категории {}", catId);

        Category category = CategoryMapper.mapperToCategory(getCategoryById(catId));
        category.setName(dto.getName());
        return CategoryMapper.mapperToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategoryByAdmin(long catId) {
        log.info("Обрабатываем запрос на удаление категории {}", catId);

        exceptionIfNotCategory(catId);
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            log.warn("Категория {} не пустая", catId);
            throw new ForbiddenArgumentException("Категория не пустая");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        log.info("Обрабатываем запрос на просмотр всех категорий");

        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::mapperToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        log.info("Обрабатываем запрос на просмотр категории с id {}", catId);
        return CategoryMapper.mapperToCategoryDto(exceptionIfNotCategory(catId));
    }

    private Category exceptionIfNotCategory(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.warn("Категории с id {} не существует", catId);
                    return new CompilationNotFoundException(String.format("Категории с id %d не существует.", catId));
                });
    }
}
