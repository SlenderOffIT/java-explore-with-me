package ru.practicum.categories.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto postCategoryByAdmin(@Valid @RequestBody CategoryDto category) {
        log.info("Поступил запрос на создание категории {}", category);
        return categoryService.addCategoryByAdmin(category);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategoryByAdmin(@PathVariable(name = "catId") long catId,
                                             @Valid @RequestBody CategoryDto category) {
        log.info("Поступил запрос на обновление категории с id {} {}", catId, category);
        return categoryService.updateCategoryByAdmin(catId, category);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryByAdmin(@PathVariable(name = "catId") long catId) {
        log.info("Поступил запрос на удаление категории {}", catId);
        categoryService.deleteCategoryByAdmin(catId);
    }
}
