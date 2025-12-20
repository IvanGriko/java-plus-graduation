package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.CompilationPublicApi;
import ru.practicum.compilation.service.CompilationPublicService;
import ru.practicum.dto.compilation.CompilationDto;

import java.util.Collection;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController implements CompilationPublicApi {

    private final CompilationPublicService compilationPublicService;

    @Override
    public Collection<CompilationDto> getCompilation(Boolean pinned, int from, int size) {
        log.info("Получение списка компиляций");
        return compilationPublicService.readAllCompilations(pinned, from, size);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение компиляции с ID {}", compId);
        return compilationPublicService.readCompilationById(compId);
    }
}