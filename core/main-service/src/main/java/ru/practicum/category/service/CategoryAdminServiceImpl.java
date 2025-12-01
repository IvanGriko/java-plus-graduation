package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = false)
public class CategoryAdminServiceImpl implements CategoryAdminService {

    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(CategoryDto requestCategory) {
        log.info("Создание новой категории: {}", requestCategory.getName());
        if (categoryRepository.existsByName(requestCategory.getName())) {
            log.error("Категория с таким названием уже существует: {}", requestCategory.getName());
            throw new ConflictException("Категория с таким названием уже существует");
        }
        Category createdCategory = categoryRepository.save(CategoryMapper.toCategories(requestCategory));
        log.info("Категория '{}' успешно создана", createdCategory.getName());
        return CategoryMapper.toCategoryDto(createdCategory);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с ID={}", catId);
        if (!categoryRepository.existsById(catId)) {
            log.error("Категория с ID={} не найдена", catId);
            throw new NotFoundException("Категория с указанным ID не найдена");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            log.error("Нельзя удалить категорию, потому что с ней связаны события");
            throw new ConflictException("Нельзя удалить категорию, потому что с ней связаны события");
        }
        categoryRepository.deleteById(catId);
        log.info("Категория с ID={} успешно удалена", catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с ID={}: {}", catId, categoryDto.getName());
        Category existingCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с указанным ID не найдена"));
        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            log.error("Категория с таким названием уже существует: {}", categoryDto.getName());
            throw new ConflictException("Категория с таким названием уже существует");
        }
        existingCategory.setName(categoryDto.getName());
        categoryRepository.save(existingCategory);
        log.info("Категория с ID={} успешно обновлена", catId);
        return CategoryMapper.toCategoryDto(existingCategory);
    }
}
