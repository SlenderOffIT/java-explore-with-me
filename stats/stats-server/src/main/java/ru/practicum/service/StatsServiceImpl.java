package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.DateException;
import ru.practicum.model.Stat;
import ru.practicum.model.mapper.HitMapper;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    public void postHit(HitDto hitDto) {
        log.info("Обрабатываем запрос на сохранение Hit {}", hitDto);
        repository.save(HitMapper.toHit(hitDto));
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Обрабатываем на просмотр со start {} и end {}", start, end);

        if (start.isAfter(end)) {
            log.warn("start не может быть позже end");
            throw new DateException("start не может быть позже end");
        }
        List<Stat> result;
        if (uris == null) {
            if (unique) {
                result = repository.findAllUniqueIp(start, end);
            } else {
                result = repository.findAllNotUniqueIp(start, end);
            }
        } else {
            if (unique) {
                result = repository.findUniqueIpByUris(start, end, uris);
            } else {
                result = repository.findNotUniqueIpByUris(start, end, uris);
            }
        }
        return result.stream()
                .map(StatMapper::toDto)
                .collect(Collectors.toList());
    }
}
