package org.knit241;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CityViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSearchOnHomePage() throws Exception {
        mockMvc.perform(get("/search").param("query", "Tokyo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tokyo")));
    }

    @Test
    void testCityPage() throws Exception {
        String mainPage = mockMvc.perform(get("/"))
                .andReturn().getResponse().getContentAsString();
        Pattern pattern = Pattern.compile("/city/(\\d+)");
        Matcher matcher = pattern.matcher(mainPage);
        assertTrue(matcher.find(), "Ссылка на город не найдена на главной странице!");
        String cityId = matcher.group(1);

        mockMvc.perform(get("/city/" + cityId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Население")));
    }

    @Test
    void testNotFoundCity() throws Exception {
        mockMvc.perform(get("/city/999999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ошибка")));
    }
}
