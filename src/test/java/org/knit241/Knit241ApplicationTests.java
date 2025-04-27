package org.knit241;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CityControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	@DisplayName("GET /api/cities – список всех городов")
	void shouldReturnAllCities() throws Exception {
		mvc.perform(get("/api/cities"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThan(0))))
				.andExpect(jsonPath("$[0].city", not(emptyString())));
	}

	@Test
	@DisplayName("GET /api/cities/Moscow – проверка полей Москвы")
	void shouldReturnMoscow() throws Exception {
		mvc.perform(get("/api/cities/Moscow"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city", is("Moscow")))
				.andExpect(jsonPath("$.country", is("Russia")))
				.andExpect(jsonPath("$.timezone", is("Europe/Moscow")));
	}

	@Test
	@DisplayName("GET /api/cities/country/Russia – города России")
	void shouldReturnRussianCities() throws Exception {
		mvc.perform(get("/api/cities/country/Russia"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].country", everyItem(is("Russia"))));
	}

	@Test
	@DisplayName("GET /api/cities/timezone/Europe/Paris – города по поясу Europe/Paris")
	void shouldReturnEuropeParisTz() throws Exception {
		mvc.perform(get("/api/cities/timezone/Europe/Paris"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].timezone", everyItem(is("Europe/Paris"))));
	}

	@Test
	@DisplayName("GET /api/cities/time/Tokyo – только время для Токио")
	void shouldReturnTimeForTokyo() throws Exception {
		mvc.perform(get("/api/cities/time/Tokyo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localTime", not(emptyString())))
				.andExpect(jsonPath("$.utcTime", startsWith("20")));
	}

	@ParameterizedTest(name = "Smoke test {index}: {0}")
	@CsvSource({
			"/api/cities",
			"/api/cities/Moscow",
			"/api/cities/country/Russia",
			"/api/cities/timezone/Europe/Paris",
			"/api/cities/time/Tokyo"
	})
	void smokeTest(String uri) throws Exception {
		mvc.perform(get(uri))
				.andExpect(status().isOk());
	}
}