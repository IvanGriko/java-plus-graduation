package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryPublicService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/categories")
@Slf4j
public class CategoryPublicController {

    private final CategoryPublicService service;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> readAllCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.info("GET запрос на получение всех категорий: from={}, size={}", from, size);
        List<CategoryDto> categories = service.readAllCategories(from, size);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> readCategoryById(
            @PathVariable Long catId
    ) {
        log.info("GET запрос на получение категории с ID={}", catId);
        CategoryDto category = service.readCategoryById(catId);
        if (category == null) {
            log.warn("Категория с ID={} не найдена", catId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }
}
