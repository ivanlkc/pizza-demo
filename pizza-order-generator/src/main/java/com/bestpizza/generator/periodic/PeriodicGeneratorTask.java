package com.bestpizza.generator.periodic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.bestpizza.api.model.Order;
import com.bestpizza.api.model.Pizza;
import com.bestpizza.generator.BackendConfig;

import reactor.core.publisher.Mono;

/**
 * Using the low privilege client, automatically send a random pizza order every
 * 30 seconds; And using the admin account, automatically send a random pizza
 * order + query total orders every 60 seconds. The intervals can be changed in
 * application.properties.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class PeriodicGeneratorTask {
	@Autowired
	private BackendConfig backendConfig;

	@Autowired
	@Qualifier("CustomerWebClient")
	private WebClient customerWebClient;

	@Autowired
	@Qualifier("AdminUserWebClient")
	private WebClient adminUserWebClient;

	private static final Random r = new Random();

	private static final String[] pizzaNames = new String[] { "Veggie", "Pepperoni", "Margherita", "BBQ", "Hawaiian",
			"Buffalo", "Chicago" };
	private static final String[] toppingNames = new String[] { "Mushroom", "Cheese", "Sausage", "Onion", "Pepper",
			"Tomato", "Garlic", "Bacon" };

	@Scheduled(initialDelayString = "${pizza.customer.repeat-interval-in-seconds}",
			fixedDelayString = "${pizza.customer.repeat-interval-in-seconds}", timeUnit = TimeUnit.SECONDS)
	public void placeOrderAsNormalClient() {
		log.info("Normal user placing order to {}", backendConfig.getAddress());
		this.fireCreateOrderRequestToBackEnd(customerWebClient);
	}

	@Scheduled(initialDelayString = "${pizza.admin.repeat-interval-in-seconds}",
			fixedDelayString = "${pizza.admin.repeat-interval-in-seconds}", timeUnit = TimeUnit.SECONDS)
	public void placeOrderAsAdmin() {
		log.info("Admin placing order to {}", backendConfig.getAddress());
		this.fireCreateOrderRequestToBackEnd(adminUserWebClient);
		log.info("Admin trying to retrieve all orders...");
		// Query all orders to demonstrate the OAuth2 Scope is effective.
		// This can be proven by replacing adminUserWebClient with customerWebClient.
		// Then the following request will fail due to insufficient OAuth2 scope.
		adminUserWebClient.get().uri(backendConfig.getAddress()).retrieve().onStatus(HttpStatus::isError, response -> {
			final HttpStatus status = response.statusCode();
			log.error("Error received: {} {}", status, response.headers().asHttpHeaders());
			response.bodyToMono(String.class)
					.subscribe(serverMessage -> log.error("Error body was: {}", serverMessage));
			return Mono.error(new IllegalStateException("Failed with " + String.valueOf(status.value())));
		}).onStatus(HttpStatus::is2xxSuccessful, response -> {
			log.info("Success! {}", response.statusCode());
			return Mono.empty();
		}).bodyToMono(new ParameterizedTypeReference<List<Order>>() {
		}).timeout(backendConfig.getRequestTimeout())
				.subscribe(answer -> log.info("Total {} order returned from server!", answer.size()));
	}

	private void fireCreateOrderRequestToBackEnd(WebClient wc) {
		wc.post().uri(backendConfig.getAddress()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(generateRandomOrderForCreate()).retrieve().onStatus(HttpStatus::isError, response -> {
					final HttpStatus status = response.statusCode();
					log.error("Error received: {} {}", status, response.headers().asHttpHeaders());
					response.bodyToMono(String.class)
							.subscribe(serverMessage -> log.error("Error body was: {}", serverMessage));
					return Mono.error(new IllegalStateException("Failed with " + String.valueOf(status.value())));
				}).onStatus(HttpStatus::is2xxSuccessful, response -> {
					log.info("Success! {}", response.statusCode());
					return Mono.empty();
				}).bodyToMono(Order.class).timeout(backendConfig.getRequestTimeout())
				.subscribe(answer -> log.info("Returned order from server is: {}", answer));
	}

	/**
	 * Randomly make up some values into a new Order.
	 */
	private static Order generateRandomOrderForCreate() {
		final Order result = new Order();
		result.setPrice(20 + r.nextInt(400));
		final List<Pizza> pizzas = new ArrayList<>();
		for (int i = 0; i <= r.nextInt(3); i++) {
			final Pizza p = new Pizza();
			p.setName(pizzaNames[r.nextInt(pizzaNames.length)]);
			p.setQuantity(1 + r.nextInt(4));
			p.setSize(1 + r.nextInt(3));
			final int toppingCount = r.nextInt(5);
			if (toppingCount > 0) {
				final List<String> toppings = new ArrayList<>();
				ThreadLocalRandom.current().ints(0, toppingNames.length).distinct().limit(toppingCount)
						.forEach(x -> toppings.add(toppingNames[x]));
				p.setToppings(toppings);
			}
			pizzas.add(p);
		}
		result.setPizzas(pizzas);
		return result;
	}
}
