package stats.model.mappers;

import stats.StatsDto;
import stats.model.Stat;

public class MapperStat {

    public static StatsDto mapperToStatsDto(Stat stat) {
        return StatsDto.builder()
                .app(stat.getApp())
                .hits(stat.getHits())
                .uri(stat.getUri())
                .build();
    }
}
