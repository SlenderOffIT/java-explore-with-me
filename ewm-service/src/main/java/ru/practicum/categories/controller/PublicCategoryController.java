package ru.practicum.categories.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategoriesPublic(@RequestParam(name = "from", defaultValue = "0", required = false) int from,
                                                    @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        log.info("Поступил запрос на просмотр всех категорий");
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getByIdPublic(@PathVariable(name = "catId") long catId) {
        log.info("Поступил запрос на просмотр категории {}", catId);
        return categoryService.getCategoryById(catId);
    }
}
