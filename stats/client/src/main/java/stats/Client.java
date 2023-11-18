package stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class Client {

    private static final String HIT_ENDPOINT = "/api/hit";
    private static final String STATS_ENDPOINT = "/stats";
    private final RestTemplate restTemplate;
    @Value("${stats-server.url}")
    private String statsUri;

    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendHit(HitDto hit) {
        String url = statsUri + HIT_ENDPOINT;
        restTemplate.postForObject(url, hit, Void.class);
    }

    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String url = statsUri + STATS_ENDPOINT;

        ParameterizedTypeReference<List<StatsDto>> responseType = new ParameterizedTypeReference<List<StatsDto>>() {};
        ResponseEntity<List<StatsDto>> response = restTemplate.exchange(
                buildStatsUrl(url, start, end, uris, unique),
                HttpMethod.GET,
                null,
                responseType
        );
        return response.getBody();
    }

    private String buildStatsUrl(String baseUrl, String start, String end, List<String> uris, boolean unique) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?start=").append(start)
                .append("&end=").append(end)
                .append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(uri);
            }
        }
        return urlBuilder.toString();
    }
}