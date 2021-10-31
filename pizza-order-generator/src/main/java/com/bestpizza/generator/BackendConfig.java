package com.bestpizza.generator;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration values of the back end, including its HTTP endpoint address and
 * suitable timeout value.
 */
@Component
@ConfigurationProperties("pizza.back-end")
public class BackendConfig {
	@lombok.Getter
	@lombok.Setter
	private String address;

	@lombok.Getter
	@lombok.Setter
	private Duration requestTimeout;
}
