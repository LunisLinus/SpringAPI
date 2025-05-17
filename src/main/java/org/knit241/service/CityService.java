package org.knit241.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.knit241.model.CityInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CityService {

    private final List<CityInfo> cities = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private long idCounter = 1;

    private static final List<String> FALLBACK_IMAGES = List.of(
            "https://images.unsplash.com/photo-1464983953574-0892a716854b",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
            "https://images.unsplash.com/photo-1502602898657-3e91760cbb34"
    );

    @Value("${unsplash.access-key:}")
    private String unsplashAccessKey;

    private static final DateTimeFormatter LOCAL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cities.csv")),
                StandardCharsets.UTF_8))) {
            reader.lines().skip(1).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    long population = 100_000L + new Random().nextInt(9_000_000);
                    String imageUrl = getImageUrl(parts[0]);
                    cities.add(CityInfo.builder()
                            .id(idCounter++)
                            .city(parts[0])
                            .country(parts[1])
                            .latitude(Double.parseDouble(parts[2]))
                            .longitude(Double.parseDouble(parts[3]))
                            .timezone(parts[4])
                            .population(population)
                            .imageUrl(imageUrl)
                            .build());
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить файл cities.csv", e);
        }
    }

    public List<CityInfo> getAllCities() {
        return cities.stream().map(this::withTime).toList();
    }

    public List<CityInfo> searchCities(String query) {
        String q = query.toLowerCase();
        return cities.stream()
                .filter(city -> city.getCity().toLowerCase().contains(q)
                        || city.getCountry().toLowerCase().contains(q)
                        || city.getTimezone().toLowerCase().contains(q))
                .map(this::withTime)
                .toList();
    }

    @Cacheable("unsplashImages")
    public String getImageUrl(String cityName) {
        if (unsplashAccessKey == null || unsplashAccessKey.isBlank()) {
            return getFallbackImage(cityName);
        }

        try {
            String url = "https://api.unsplash.com/search/photos?query=" + cityName +
                    "&client_id=" + unsplashAccessKey +
                    "&orientation=landscape&per_page=1";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode results = root.get("results");
                if (results != null && results.size() > 0) {
                    return results.get(0).get("urls").get("regular").asText();
                }
            }
        } catch (Exception e) {
            System.out.println("Unsplash error (" + cityName + "): " + e.getMessage());
        }

        return getFallbackImage(cityName);
    }

    private String getFallbackImage(String cityName) {
        int idx = Math.abs(cityName.hashCode()) % FALLBACK_IMAGES.size();
        return FALLBACK_IMAGES.get(idx);
    }

    private CityInfo withTime(CityInfo city) {
        try {
            ZoneId zoneId = ZoneId.of(city.getTimezone());
            ZonedDateTime local = ZonedDateTime.now(zoneId);
            String localStr = LOCAL_FMT.format(local);
            String utcStr = Instant.now().toString();
            String desc = String.format("%s: %s (%s UTC)",
                    city.getCity(),
                    local.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    offsetStr(local.getOffset()));
            return CityInfo.builder()
                    .id(city.getId())
                    .city(city.getCity())
                    .country(city.getCountry())
                    .latitude(city.getLatitude())
                    .longitude(city.getLongitude())
                    .timezone(city.getTimezone())
                    .population(city.getPopulation())
                    .imageUrl(city.getImageUrl())
                    .localTime(localStr)
                    .utcTime(utcStr)
                    .timeDescription(desc)
                    .build();
        } catch (Exception e) {
            return city;
        }
    }

    private String offsetStr(ZoneOffset offset) {
        int seconds = offset.getTotalSeconds();
        int hours = seconds / 3600;
        return String.format("%+d", hours);
    }

    public CityInfo getByName(String name) {
        return cities.stream()
                .filter(c -> c.getCity().equalsIgnoreCase(name))
                .findFirst()
                .map(this::withTime)
                .orElse(null);
    }

    public List<CityInfo> findByCountry(String country) {
        return cities.stream()
                .filter(c -> c.getCountry().equalsIgnoreCase(country))
                .map(this::withTime)
                .collect(Collectors.toList());
    }

    public List<CityInfo> findByTimezone(String tz) {
        return cities.stream()
                .filter(c -> c.getTimezone().equalsIgnoreCase(tz))
                .map(this::withTime)
                .collect(Collectors.toList());
    }

    public Map<String, String> getTimeOnly(String name) {
        CityInfo city = getByName(name);
        if (city == null) return Map.of("error", "City not found: " + name);
        return Map.of(
                "localTime", city.getLocalTime(),
                "utcTime", city.getUtcTime()
        );
    }

    public CityInfo getByCityId(long id) {
        return cities.stream()
                .filter(city -> city.getId() == id)
                .findFirst()
                .map(this::withTime)
                .orElse(null);
    }
}
