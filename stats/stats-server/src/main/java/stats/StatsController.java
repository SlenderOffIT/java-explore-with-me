package stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import stats.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private final StatService service;

    @PostMapping("/hit")
    public ResponseEntity<?> postHit(@RequestBody HitDto hitDto, UriComponentsBuilder uriComponentsBuilder) {
        log.info("Поступил запрос на создание Hit {}", hitDto);
        return service.postHit(hitDto, uriComponentsBuilder);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                      @RequestParam(required = false) List<String> uris,
                                      @RequestParam(defaultValue = "false", required = false) boolean unique) {
        log.info("Поступил запрос на просмотр со start {} и end {}", start, end);
        return service.getStats(start, end, uris, unique);
    }
}
