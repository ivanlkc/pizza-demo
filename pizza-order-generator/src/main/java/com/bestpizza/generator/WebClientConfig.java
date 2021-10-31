package com.bestpizza.generator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configure a Spring WebClient which is able to handle Oauth2 tokens while
 * sending requests to the secured back end.
 */
@Configuration
public class WebClientConfig {
	/**
	 * This bean retrieve the client details provided by
	 * "spring.security.oauth2.client.registration.*" in the properties files.
	 */
	@Bean
	InMemoryReactiveClientRegistrationRepository myClientRegistrationRepo(OAuth2ClientProperties properties) {
		List<ClientRegistration> registrations = new ArrayList<>(
				OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values());
		return new InMemoryReactiveClientRegistrationRepository(registrations);
	}

	/**
	 * This corresponds to the low privilege customer hard-coded in the
	 * Authorization Server.
	 */
	@Bean("CustomerWebClient")
	WebClient customerWebClient(ReactiveClientRegistrationRepository clientRegistrations) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				clientRegistrations, new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
		oauth.setDefaultClientRegistrationId("low-level-customer1");
		return WebClient.builder().filter(oauth).build();
	}

	/**
	 * This corresponds to the admin privilege user hard-coded in the Authorization
	 * Server.
	 */
	@Bean("AdminUserWebClient")
	WebClient adminUserWebClient(ReactiveClientRegistrationRepository clientRegistrations) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				clientRegistrations, new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
		oauth.setDefaultClientRegistrationId("admin-user2");
		return WebClient.builder().filter(oauth).build();
	}
}
