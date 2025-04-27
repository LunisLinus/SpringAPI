package org.knit241.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.knit241.model.CityInfo;
import org.knit241.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService service;
    @GetMapping
    public List<CityInfo> all() {
        return service.getAllCities();
    }

    @GetMapping("/{name}")
    public CityInfo byName(@PathVariable String name) {
        return service.getByName(name);
    }

    @GetMapping("/country/{country}")
    public List<CityInfo> byCountry(@PathVariable String country) {
        return service.findByCountry(country);
    }

    @GetMapping("/timezone/**")
    public List<CityInfo> byTimezone(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String prefix = "/api/cities/timezone/";
        String timezone = fullPath.substring(fullPath.indexOf(prefix) + prefix.length());
        return service.findByTimezone(timezone);
    }

    @GetMapping("/time/{name}")
    public Map<String, String> timeOf(@PathVariable String name) {
        return service.getTimeOnly(name);
    }
}