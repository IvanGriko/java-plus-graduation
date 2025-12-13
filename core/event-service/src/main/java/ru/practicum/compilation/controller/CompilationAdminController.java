package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.CompilationAdminApi;
import ru.practicum.compilation.service.CompilationAdminService;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController implements CompilationAdminApi {

    private final CompilationAdminService compilationAdminService;

    @Override
    public CompilationDto postCompilations(NewCompilationDto newCompilationDto) {
        log.info("Создан новый сборник {}", newCompilationDto.getTitle());
        return compilationAdminService.createCompilation(newCompilationDto);
    }

    @Override
    public String deleteCompilation(Long compId) {
        log.info("Удаление компиляции с ID {}", compId);
        return compilationAdminService.deleteCompilation(compId);
    }

    @Override
    public CompilationDto patchCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        log.info("Обновление компиляции с ID {}", compId);
        return compilationAdminService.updateCompilation(compId, updateCompilationDto);
    }

}