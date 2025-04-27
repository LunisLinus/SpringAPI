package org.knit241.service;

import org.knit241.model.CityInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CityService {

    private final List<CityInfo> cities = new ArrayList<>();
    private static final DateTimeFormatter LOCAL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("geonames_cities.csv")),
                StandardCharsets.UTF_8))) {
            reader.lines().skip(1).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    cities.add(CityInfo.builder()
                            .city(parts[0])
                            .country(parts[1])
                            .latitude(Double.parseDouble(parts[2]))
                            .longitude(Double.parseDouble(parts[3]))
                            .timezone(parts[4])
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
                .toList();
    }

    public List<CityInfo> findByTimezone(String tz) {
        return cities.stream()
                .filter(c -> c.getTimezone().equalsIgnoreCase(tz))
                .map(this::withTime)
                .toList();
    }

    public Map<String, String> getTimeOnly(String name) {
        CityInfo city = getByName(name);
        if (city == null) return Map.of(
                "error", "City not found: " + name);
        return Map.of(
                "localTime", city.getLocalTime(),
                "utcTime", city.getUtcTime());
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
                    .city(city.getCity())
                    .country(city.getCountry())
                    .latitude(city.getLatitude())
                    .longitude(city.getLongitude())
                    .timezone(city.getTimezone())
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
}