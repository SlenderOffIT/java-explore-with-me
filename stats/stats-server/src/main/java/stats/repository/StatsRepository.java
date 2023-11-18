package stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stats.model.Hit;
import stats.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("select new stats.model.Stat(h.app as app, h.uri as uri, count(distinct h.ip) as hits)" +
            "from Hit h " +
            "where h.timestamp between :start and :end " +
            "and ((:uris) is null or h.uri in :uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<Stat> findUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new stats.model.Stat(h.app as app, h.uri as uri, count(h.ip) as hits)" +
            "from Hit h " +
            "where h.timestamp between :start and :end " +
            "and ((:uris) is null or h.uri in :uris) " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<Stat> findNotUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);

}