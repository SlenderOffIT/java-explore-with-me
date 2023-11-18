package stats.model.mappers;

import stats.HitDto;
import stats.model.Hit;

public class MapperHit {

    public static Hit mapperToHit(HitDto hitDto) {
        return Hit.builder()
                .uri(hitDto.getUri())
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
