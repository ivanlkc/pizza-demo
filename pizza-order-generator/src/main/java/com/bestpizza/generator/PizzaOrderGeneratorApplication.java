package com.bestpizza.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is the Spring Boot entry point class of this Pizza Order Generator
 * Service.
 */
@SpringBootApplication
@EnableScheduling
public class PizzaOrderGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaOrderGeneratorApplication.class, args);
	}

}
