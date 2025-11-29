package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryAdminService;
import ru.practicum.validation.CreateOrUpdateValidator;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/categories")
@Slf4j
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    public ResponseEntity<?> addCategory(
            @RequestBody @Validated(CreateOrUpdateValidator.Create.class) CategoryDto requestCategory,
            BindingResult bindingResult
    ) {
        log.info("POST запрос на добавление категории: {}", requestCategory);
        if (bindingResult.hasErrors()) {
            log.error("Ошибка валидации при создании категории: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryAdminService.createCategory(requestCategory));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategories(
            @PathVariable @Positive Long catId
    ) {
        log.info("DELETE запрос на удаление категории с ID={}", catId);
        categoryAdminService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @RequestBody @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
    ) {
        log.info("PATCH запрос на обновление категории с ID={}, обновляемые данные: {}", catId, categoryDto);
        return ResponseEntity.ok(categoryAdminService.updateCategory(catId, categoryDto));
    }

    private ResponseEntity<List<ObjectError>> validationError(BindingResult bindingResult) {
        return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }
}

//@RestController
//@RequiredArgsConstructor
//@Validated
//@RequestMapping(path = "/admin/categories")
//@Slf4j
//public class CategoryAdminController {
//
//    private final CategoryAdminService categoryAdminService;
//
//    @PostMapping
//    public ResponseEntity<CategoryDto> addCategory(
//            @RequestBody @Validated(CreateOrUpdateValidator.Create.class)
//            CategoryDto requestCategory,
//            BindingResult bindingResult
//    ) {
//        log.info("Calling the POST request to /admin/categories endpoint");
//        if (bindingResult.hasErrors()) {
//            log.error("Validation error with category name");
//            return ResponseEntity.badRequest().body((requestCategory));
//        }
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(categoryAdminService.createCategory(requestCategory));
//    }
//
//    @DeleteMapping("/{catId}")
//    public ResponseEntity<String> deleteCategories(
//            @PathVariable @Positive Long catId
//    ) {
//        log.info("Calling the DELETE request to /admin/categories/{catId} endpoint");
//        categoryAdminService.deleteCategory(catId);
//        return ResponseEntity
//                .status(HttpStatus.NO_CONTENT)
//                .body("Category deleted: " + catId);
//    }
//
//    @PatchMapping("/{catId}")
//    public ResponseEntity<CategoryDto> updateCategory(
//            @PathVariable Long catId,
//            @RequestBody @Validated(CreateOrUpdateValidator.Update.class) CategoryDto categoryDto
//    ) {
//        log.info("Calling the PATCH request to /admin/categories/{catId} endpoint");
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(categoryAdminService.updateCategory(catId, categoryDto));
//    }
//}