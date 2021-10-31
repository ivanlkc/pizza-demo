package com.bestpizza.service.api.impl;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.bestpizza.api.model.Order;
import com.bestpizza.service.dao.OrderContentRepository;
import com.bestpizza.service.dao.OrderDetailRepository;
import com.bestpizza.service.dao.PizzaDetailRepository;
import com.bestpizza.service.dao.PizzaToppingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableJpaRepositories({ "com.bestpizza.service.dao" })
@EntityScan(basePackages = { "com.bestpizza.service.entity" })
@EnableTransactionManagement
@SpringBootTest(classes = { OrdersControllerTests.class, OrdersController.class })
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class,
		ManagementWebSecurityAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false, print = MockMvcPrint.DEFAULT, printOnlyOnFailure = true, webClientEnabled = false, webDriverEnabled = false)
public class OrdersControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderDetailRepository orderDetailRepository;
	@Autowired
	private OrderContentRepository orderContentRepository;
	@Autowired
	private PizzaDetailRepository pizzaDetailRepository;
	@Autowired
	private PizzaToppingRepository pizzaToppingRepository;

	@Test
	void testGetSingleNonExistOrder() throws Exception {
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.get("/v1/orders/11112222-2222-2222-2222-333344443333")).andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());

		result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/orders/XXXX")).andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());
	}

	@Test
	void testCreateInvalidOrder() throws Exception {
		// first: ID was given
		String orderToPost = "{\"id\":\"XYZ\",\"price\":29,\"pizzas\":[{\"name\":\"BBQ\",\"size\":2,\"quantity\":2,\"toppings\":"
				+ "[\"Sausage\",\"Pepper\",\"Bacon\",\"Garlic\"]}]}";
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());

		// second: no pizza
		orderToPost = "{\"id\":\"XYZ\",\"price\":29,\"pizzas\":[]}";
		result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());

		// third: no pizza name
		orderToPost = "{\"id\":null,\"price\":333,\"pizzas\":[{\"name\":\"XYZ\",\"size\":1,\"quantity\":1,\"toppings\":[\"Sausage\"]},"
				+ "{\"size\":3,\"quantity\":2}" + "]}";
		result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());

		// fourth: no quantity
		orderToPost = "{\"id\":null,\"price\":333,\"pizzas\":[{\"name\":\"XYZ\",\"size\":1,\"toppings\":[\"Sausage\"]},"
				+ "{\"name\":\"ABC\",\"size\":3,\"quantity\":2}" + "]}";
		result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(400, result.getResponse().getStatus());
	}

	@Test
	void testCreateAndThenGetOrder() throws Exception {
		// first: 1 pizza 4 topping
		String orderToPost = "{\"id\":null,\"price\":29,\"pizzas\":[{\"name\":\"BBQ\",\"size\":2,\"quantity\":2,\"toppings\":"
				+ "[\"Sausage\",\"Pepper\",\"Bacon\",\"Garlic\"]}]}";
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		Assertions.assertEquals(4, pizzaToppingRepository.findAll().size());

		UUID anyOrderID = orderDetailRepository.findAll().get(0).getOrderId();

		// second: 2 pizza 1 topping
		orderToPost = "{\"id\":null,\"price\":333,\"pizzas\":[{\"name\":\"XYZ\",\"size\":1,\"quantity\":1,\"toppings\":[\"Sausage\"]},"
				+ "{\"name\":\"ABC\",\"size\":3,\"quantity\":2}" + "]}";
		result = mockMvc.perform(
				MockMvcRequestBuilders.post("/v1/orders").contentType(MediaType.APPLICATION_JSON).content(orderToPost))
				.andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());

		// check database has correct number of entries created
		Assertions.assertEquals(5, pizzaToppingRepository.findAll().size());
		Assertions.assertEquals(3, pizzaDetailRepository.findAll().size());
		Assertions.assertEquals(3, orderContentRepository.findAll().size());
		Assertions.assertEquals(2, orderDetailRepository.findAll().size());

		// do GET and check result matches POST
		result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/v1/orders/%s", anyOrderID))).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
		ObjectMapper jsonMapper = new ObjectMapper();
		Order testOrder = jsonMapper.readValue(result.getResponse().getContentAsString(), Order.class);
		Assertions.assertEquals(29, testOrder.getPrice());
		Assertions.assertEquals("BBQ", testOrder.getPizzas().get(0).getName());

		result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/orders")).andReturn();
		Assertions.assertEquals(200, result.getResponse().getStatus());
		Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
		List<Order> allOrders = jsonMapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<List<Order>>() {
				});
		Assertions.assertEquals(2, allOrders.size());
	}
}
