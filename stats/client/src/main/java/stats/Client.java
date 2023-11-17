package stats;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class Client {

    private final RestTemplate restTemplate;
    private final String statsUri = "http://stats-server:9090";

    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendHit(HitDto hit) {
        String url = statsUri + "/api/hit";
        restTemplate.postForObject(url, hit, Void.class);
    }

    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String url = statsUri + "/api/stats";

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