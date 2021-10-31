package com.bestpizza.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is the Spring Boot entry point class of this Pizza Order Back End
 * Service.
 */
@SpringBootApplication
@EnableTransactionManagement
public class PizzaBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaBackEndApplication.class, args);
	}

}
