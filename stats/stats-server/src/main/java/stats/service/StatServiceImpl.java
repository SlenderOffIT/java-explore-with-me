package stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import stats.HitDto;
import stats.exception.DateException;
import stats.model.mappers.MapperHit;
import stats.model.mappers.MapperStat;
import stats.model.Stat;
import stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatsRepository statsRepository;

    @Override
    public ResponseEntity<?> postHit(HitDto hitDto, UriComponentsBuilder uriComponentsBuilder) {
        log.info("Обрабатываем запрос на сохранение Hit {}", hitDto);

        var hit =  statsRepository.save(MapperHit.mapperToHit(hitDto));
        return ResponseEntity.created(uriComponentsBuilder
                        .path("/hit/{hitId}")
                        .build(Map.of("hitId", hit.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(hit);
    }

    @Override
    public ResponseEntity<?> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Обрабатываем на просмотр со start {} и end {}", start, end);

        List<Stat> result;

        // Николай, увидел такую реализацию обработки ошибок, да и сама реализация методов, хотел узнать
        // правильнее будет так использовать или как в прошлых спринтах, и обработка ошибок, так или через ErrorHandler?
        if (start == null || end == null) {
            log.warn("badRequest, start {} или end {} не задано", start, end);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DateException("Параметры start должно быть заполнено"));
        }
        if (start.isAfter(end)) {
            log.warn("badRequest, start {} позже end {}", start, end);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DateException("Параметры start должно быть заполнено"));
        }

        if (unique) {
            log.info("На уникальные посещения unique {} и список uris {}", unique, uris);
            result = statsRepository.findUniqueStats(start, end, uris);
        } else {
            log.info("На не уникальные посещения unique {} и список uris {}", unique, uris);
            result = statsRepository.findNotUniqueStats(start, end, uris);
        }

        log.info("Выводим результат {}", result);
        return ResponseEntity.ok(result.stream()
                .map(MapperStat::mapperToStatsDto)
                .collect(Collectors.toList()));
    }
}

