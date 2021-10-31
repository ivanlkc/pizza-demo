package com.bestpizza.generator.periodic;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.bestpizza.api.model.Order;
import com.bestpizza.generator.BackendConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(classes = { PeriodicGeneratorTaskTests.Config.class, PeriodicGeneratorTaskTests.class,
		PeriodicGeneratorTask.class })
class PeriodicGeneratorTaskTests {
	@TestConfiguration
	static class Config {
		@MockBean
		BackendConfig backendConfig;
	}

	@Autowired
	BackendConfig backendConfig;

	@Bean("CustomerWebClient")
	WebClient customerWebClient() {
		return WebClient.create();
	}

	@Bean("AdminUserWebClient")
	WebClient adminUserWebClient() {
		return WebClient.create();
	}

	@Autowired
	private PeriodicGeneratorTask periodicGeneratorTask;

	private static MockWebServer mockBackEnd;

	@BeforeAll
	static void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackEnd.shutdown();
	}

	@Test
	void testPlaceOrderAsNormalClient() throws InterruptedException, JsonMappingException, JsonProcessingException {
		Mockito.doReturn(String.format("http://localhost:%s", mockBackEnd.getPort())).when(backendConfig).getAddress();
		Mockito.doReturn(Duration.ofMinutes(1)).when(backendConfig).getRequestTimeout();
		periodicGeneratorTask.placeOrderAsNormalClient();
		RecordedRequest recordedRequest = mockBackEnd.takeRequest();

		// Assert a JSON Order message is correctly posted
		Assertions.assertEquals("POST", recordedRequest.getMethod());
		Assertions.assertEquals("/", recordedRequest.getPath());
		Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE));
		ObjectMapper jsonMapper = new ObjectMapper();
		Order testOrder = jsonMapper.readValue(recordedRequest.getBody().readUtf8(), Order.class);
		Assertions.assertNotNull(testOrder);
		Assertions.assertNotNull(testOrder.getPizzas());
		Assertions.assertNotNull(testOrder.getPrice());
		Assertions.assertNull(testOrder.getId());
	}

	@Test
	void testPlaceOrderAsAdmin() throws InterruptedException, JsonMappingException, JsonProcessingException {
		Mockito.doReturn(String.format("http://localhost:%s", mockBackEnd.getPort())).when(backendConfig).getAddress();
		Mockito.doReturn(Duration.ofMinutes(1)).when(backendConfig).getRequestTimeout();
		periodicGeneratorTask.placeOrderAsAdmin();
		RecordedRequest recordedRequest = mockBackEnd.takeRequest();

		// Assert a JSON Order message is correctly posted
		Assertions.assertEquals("POST", recordedRequest.getMethod());
		Assertions.assertEquals("/", recordedRequest.getPath());
		Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE));
		ObjectMapper jsonMapper = new ObjectMapper();
		Order testOrder = jsonMapper.readValue(recordedRequest.getBody().readUtf8(), Order.class);
		Assertions.assertNotNull(testOrder);
		Assertions.assertNotNull(testOrder.getPizzas());
		Assertions.assertNotNull(testOrder.getPrice());
		Assertions.assertNull(testOrder.getId());

		recordedRequest = mockBackEnd.takeRequest();

		// Assert a GET request is sent to get all orders
		Assertions.assertEquals("GET", recordedRequest.getMethod());
		Assertions.assertEquals("/", recordedRequest.getPath());
	}
}
