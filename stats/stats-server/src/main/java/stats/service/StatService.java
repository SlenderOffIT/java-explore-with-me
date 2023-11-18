package stats.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import stats.HitDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    ResponseEntity<?> postHit(HitDto dto, UriComponentsBuilder uriComponentsBuilder);

    ResponseEntity<?> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
