package com.bestpizza.authserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Just ensure the ApplicationContext can load. This authorization server only
 * serve as a demo to illustrate how the Pizza Ordering API is secured, so we
 * are not wasting time to unit test it.
 */
@SpringBootTest
class AuthServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
