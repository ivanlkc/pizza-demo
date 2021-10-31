package com.bestpizza.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the Spring Boot entry point class of this Pizza H2 Database.
 */
@SpringBootApplication
public class PizzaDbApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaDbApplication.class, args);
	}

}
