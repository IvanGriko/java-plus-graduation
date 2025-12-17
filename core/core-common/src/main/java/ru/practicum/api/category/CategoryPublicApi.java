package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.category.CategoryDto;

import java.util.Collection;

public interface CategoryPublicApi {

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    Collection<CategoryDto> readAllCategories(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Индекс смещения должен быть неотрицательным числом.")
            int from,
            @RequestParam(defaultValue = "10")
            @Positive(message = "Размер страницы должен быть положительным числом.")
            int size
    );

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    CategoryDto readCategoryById(
            @PathVariable
            @Positive(message = "Идентификатор категории должен быть положительным числом.")
            Long catId
    );

}