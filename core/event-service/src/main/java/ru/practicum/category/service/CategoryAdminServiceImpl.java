package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.Category;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryAdminServiceImpl implements CategoryAdminService {

    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto requestCategory) {
        log.info("Начинается создание категории");
        if (categoryRepository.existsByName(requestCategory.getName())) {
            log.error("Категорию с именем {} невозможно создать, так как она уже существует", requestCategory.getName());
            throw new ConflictException("Категория с таким именем уже существует");
        }
        Category result = categoryRepository.saveAndFlush(CategoryMapper.toCategories(requestCategory));
        log.info("Категория с именем {} создана успешно", result.getName());
        return CategoryMapper.toCategoryDto(result);
    }

    @Override
    @Transactional
    public String deleteCategory(Long catId) {
        log.info("Начинается удаление категории с ID {}", catId);
        if (!categoryRepository.existsById(catId)) {
            log.error("Категория с ID {} не найдена", catId);
            throw new NotFoundException("Категория с таким ID не найдена");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            log.error("Категория с ID {} не может быть удалена, так как существуют связанные события", catId);
            throw new ConflictException("Невозможно удалить категорию с ассоциированными событиями");
        }
        categoryRepository.deleteById(catId);
        log.info("Категория с ID {} успешно удалена", catId);
        return "Категория с ID " + catId + " успешно удалена";
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Начинается обновление категории с ID {}", catId);
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория с таким ID не найдена"));
        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            log.error("Категорию с именем {} невозможно обновить, так как такое имя уже занято", categoryDto.getName());
            throw new ConflictException("Категория с таким именем уже существует");
        }
        category.setName(categoryDto.getName());
        log.info("Категория с ID {} успешно обновлена", catId);
        return CategoryMapper.toCategoryDto(category);
    }

}