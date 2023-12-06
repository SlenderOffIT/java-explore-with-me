package ru.practicum.categories.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.model.Category;

@UtilityClass
public class CategoryMapper {
    public static CategoryDto mapperToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category mapperToCategory(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
