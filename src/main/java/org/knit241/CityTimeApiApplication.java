package org.knit241;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CityTimeApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(CityTimeApiApplication.class, args);
	}
}