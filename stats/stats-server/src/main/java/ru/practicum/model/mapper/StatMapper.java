package ru.practicum.model.mapper;

import ru.practicum.StatsDto;
import ru.practicum.model.Stat;

public class StatMapper {
    public static StatsDto toDto(Stat stat) {
        return StatsDto.builder()
                .hits(stat.getHits())
                .app(stat.getApp())
                .uri(stat.getUri())
                .build();
    }
}
