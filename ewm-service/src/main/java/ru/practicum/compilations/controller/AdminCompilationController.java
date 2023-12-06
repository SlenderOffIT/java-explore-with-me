package ru.practicum.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequestDto;
import ru.practicum.compilations.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto postByAdmin(@Valid @RequestBody NewCompilationDto dto) {
        log.info("Поступил запрос на добавление новой подборки {}", dto);
        return compilationService.add(dto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable long compId) {
        log.info("Поступил запрос на удаление подборки {}", compId);
        compilationService.delete(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateByAdmin(@PathVariable long compId, @Valid @RequestBody UpdateCompilationRequestDto dto) {
        log.info("Поступил запрос на изменение подборки {}", compId);
        return compilationService.update(compId, dto);
    }
}
