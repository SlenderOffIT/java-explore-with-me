package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.mapper.CompilationMapper;
import ru.practicum.compilations.dto.UpdateCompilationRequestDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.CompilationNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilationsPublic(Boolean pinned, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned == null) {
            log.info("Обрабатываем запрос на просмотр всех подборок");
            return compilationRepository.findAll(pageable).stream()
                    .map(CompilationMapper::mapperToCompilationDto)
                    .collect(Collectors.toList());
        }

        log.info("Обрабатываем запрос на просмотр всех подборок с pinned {}", pinned);
        return compilationRepository.findAllByPinned(pinned, pageable).stream()
                .map(CompilationMapper::mapperToCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationByIdPublic(long compId) {
        log.info("Обрабатываем запрос на просмотр события {}", compId);
        return CompilationMapper.mapperToCompilationDto(exceptionIfNotCompilation(compId));
    }

    @Override
    public CompilationDto add(NewCompilationDto dto) {
        log.info("Обрабатываем запрос на добавление подборки событий {}", dto);

        Compilation compilation = Compilation.builder()
                .events(dto.getEvents() == null ? new ArrayList<>() : eventRepository.findAllByIds(dto.getEvents()))
                .title(dto.getTitle())
                .pinned(dto.getPinned() == null ? false : dto.getPinned())
                .build();
        return CompilationMapper.mapperToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void delete(long compId) {
        log.info("Обрабатываем запрос на удаление подборки событий {}", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto update(long compId, UpdateCompilationRequestDto updateCompilationRequestDto) {
        log.info("Обрабатываем запрос на изменение подборки событий {}", compId);

        Compilation compilation = exceptionIfNotCompilation(compId);
        if (updateCompilationRequestDto.getEvents() != null) {
            log.info("Меняем список событий");
            compilation.setEvents(eventRepository.findAllByIds(updateCompilationRequestDto.getEvents()));
        }
        if (updateCompilationRequestDto.getPinned() != null) {
            log.info("Меняем закрепление подборки на главной странице");
            compilation.setPinned(updateCompilationRequestDto.getPinned());
        }
        if (updateCompilationRequestDto.getTitle() != null) {
            log.info("Меняем заголовок событий");
            compilation.setTitle(updateCompilationRequestDto.getTitle());
        }

        return CompilationMapper.mapperToCompilationDto(compilationRepository.save(compilation));
    }

    private Compilation exceptionIfNotCompilation(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Подборки событий с id {} не существует", compId);
                    return new CompilationNotFoundException(String.format("Подборки событий с id %d не существует.", compId));
                });
    }
}
