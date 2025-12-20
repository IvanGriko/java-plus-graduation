package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.Category;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicServiceImpl implements CategoryPublicService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> readAllCategories(Integer from, Integer size) {
        log.info("Начинается получение всех категорий");
        Page<Category> page = categoryRepository.findAll(PageRequest.of(from, size, Sort.Direction.ASC, "id"));
        List<Category> categories = page.getContent();
        log.info("Результат: получено {} категорий", categories.size());
        return CategoryMapper.toListCategoriesDto(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto readCategoryById(Long catId) {
        log.info("Начинается получение категории с ID {}", catId);
        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            log.error("Категория с ID {} не найдена", catId);
            return new NotFoundException("Категория с таким ID не найдена");
        });
        log.info("Результат: получена категория с именем {}", category.getName());
        return CategoryMapper.toCategoryDto(category);
    }
}