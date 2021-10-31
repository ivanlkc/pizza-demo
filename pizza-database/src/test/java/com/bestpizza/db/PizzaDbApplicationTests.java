package com.bestpizza.db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Just ensure the ApplicationContext can load. This database server only serve
 * as a demo to illustrate how the pizza data will be stored, so we are not
 * wasting time to unit test it.
 */
@SpringBootTest
class PizzaDbApplicationTests {

	@Test
	void contextLoads() {
	}

}
