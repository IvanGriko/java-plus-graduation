package ru.practicum.ewm.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EventHitDto;
import ru.practicum.dto.EventStatsResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RestStatClient implements StatClient {

    RestClient restClient;
    final String name;
    final DiscoveryClient discoveryClient;
    final Random random = new Random();
    final DateTimeFormatter formatter;
    volatile String statUrl;
    boolean enableDynamicUrlUpdate = false;

    public RestStatClient(
            DiscoveryClient discoveryClient,
            @Value("${explore-with-me.stat-server.discovery.name:}") String name,
            @Value("${explore-with-me.stat-server.url:http://localhost:9090}") String url,
            @Value("${explore-with-me.stat.datetime.format}") String format
    ) {
        this.discoveryClient = discoveryClient;
        this.name = name;
        this.formatter = DateTimeFormatter.ofPattern(format);
        this.statUrl = url;
        this.restClient = RestClient.builder().baseUrl(statUrl).build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeStatUrl() {
        if (name == null || name.isBlank()) return;
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(name);
            if (instances.isEmpty()) throw new IllegalArgumentException("Список пуст.");
            statUrl = instances.get(random.nextInt(instances.size())).getUri().toString();
            restClient = RestClient.builder().baseUrl(statUrl).build();
            log.info("Новый URL сервера статистики: {}", statUrl);
        } catch (Exception e) {
            log.warn("Ошибка при получении URL: {}", e.getMessage(), e);
        }
        enableDynamicUrlUpdate = true;
    }

    @Scheduled(fixedDelay = 60000)
    public void updateStatUrl() {
        if (!enableDynamicUrlUpdate) return;
        try {
            List<String> currentUrls = discoveryClient.getInstances(name)
                    .stream()
                    .map(si -> si.getUri().toString())
                    .filter(u -> !u.equalsIgnoreCase(statUrl))
                    .toList();
            if (currentUrls.isEmpty()) return;
            statUrl = currentUrls.get(random.nextInt(currentUrls.size()));
            restClient = RestClient.builder().baseUrl(statUrl).build();
            log.info("URL сервера статистики обновлён: {}", statUrl);
        } catch (Exception e) {
            log.warn("Ошибка обновления URL: {}", e.getMessage(), e);
        }
    }

    @Override
    public void hit(EventHitDto eventHitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(eventHitDto)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Ошибка отправки данных на сервер статистики: {}", e.getMessage());
        }
    }

    @Override
    public Collection<EventStatsResponseDto> stats(LocalDateTime start,
                                                   LocalDateTime end,
                                                   List<String> uris,
                                                   Boolean unique) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter));
            if (uris != null && !uris.isEmpty()) {
                uriBuilder.queryParam("uris", String.join(",", uris));
            }
            if (unique != null) {
                uriBuilder.queryParam("unique", unique);
            }
            return restClient.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            log.error("Ошибка получения статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String sendView(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Метод sendView() не поддерживается");
    }

    @Override
    public String sendRegister(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Метод sendRegister() не поддерживается");
    }

    @Override
    public String sendLike(Long userId, Long eventId) {
        throw new UnsupportedOperationException("Метод sendLike() не поддерживается");
    }

    @Override
    public Map<Long, Double> getUserRecommendations(Long userId, Integer size) {
        throw new UnsupportedOperationException("Метод getUserRecommendations() не поддерживается");
    }

    @Override
    public Map<Long, Double> getRatingsByEventIdList(List<Long> eventIdList) {
        throw new UnsupportedOperationException("Метод getRatingsByEventIdList() не поддерживается");
    }
}