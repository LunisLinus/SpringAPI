package org.knit241.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.knit241.exceptions.CityNotFoundException;
import org.knit241.model.CityInfo;
import org.knit241.service.CityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CityViewController {

    private final CityService cityService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        return "index";
    }

    @GetMapping("/city/{id}")
    public String cityDetails(@PathVariable long id, Model model) {
        CityInfo city = cityService.getByCityId(id);
        if (city == null) throw new CityNotFoundException();
        model.addAttribute("city", city);
        return "city";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<CityInfo> results = cityService.searchCities(query);
        model.addAttribute("cities", results);
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @ExceptionHandler(CityNotFoundException.class)
    public String handleError(HttpServletRequest request, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Город не найден.");
        return "error";
    }
}
