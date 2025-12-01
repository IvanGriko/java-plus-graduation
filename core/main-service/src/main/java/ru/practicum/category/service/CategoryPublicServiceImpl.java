package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

import static ru.practicum.util.Util.createPageRequestAsc;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryPublicServiceImpl implements CategoryPublicService {

    private final CategoryRepository repository;

    @Override
    public List<CategoryDto> readAllCategories(Integer from, Integer size) {
        log.info("Чтение всех категорий, offset={}, limit={}", from, size);
        if (from < 0 || size <= 0) {
            log.error("Недопустимые параметры пагинации: from={}, size={}", from, size);
            throw new IllegalArgumentException("Параметры пагинации должны быть положительными");
        }
        Page<Category> page = repository.findAll(createPageRequestAsc(from, size));
        List<Category> categories = page.getContent();
        log.info("Результат: получено {} категорий", categories.size());
        return CategoryMapper.toListCategoriesDto(categories);
    }

    @Override
    public CategoryDto readCategoryById(Long catId) {
        log.info("Чтение категории с ID={}", catId);
        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с ID={} не найдена", catId);
                    return new NotFoundException("Категория не найдена");
                });
        log.info("Результат: получена категория с названием {}", category.getName());
        return CategoryMapper.toCategoryDto(category);
    }
}
