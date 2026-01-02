package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.validation.CreateOrUpdateValidator;

@RequestMapping(value = "/admin/categories", produces = {"application/json"})
public interface CategoryAdminApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto addCategory(
            @RequestBody
            @Validated(CreateOrUpdateValidator.Create.class) CategoryDto requestCategory
    );

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    String deleteCategories(
            @PathVariable
            @Positive Long catId);

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    CategoryDto updateCategory(
            @PathVariable Long catId,
            @RequestBody
            @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
    );
}