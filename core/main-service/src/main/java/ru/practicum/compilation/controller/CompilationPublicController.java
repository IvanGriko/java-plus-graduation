package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationPublicService;

import java.util.List;

@RestController
@Validated
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.info("Получение подборок, pinned={}, from={}, size={}", pinned, from, size);
        List<CompilationDto> compilations = compilationPublicService.readAllCompilations(pinned, from, size);
        return ResponseEntity.ok(compilations);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(
            @PathVariable Long compId
    ) {
        log.info("Получение подборки с ID={}", compId);
        CompilationDto compilation = compilationPublicService.readCompilationById(compId);
        return ResponseEntity.ok(compilation);
    }
}
