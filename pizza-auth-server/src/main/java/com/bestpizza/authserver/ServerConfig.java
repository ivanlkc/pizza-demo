package com.bestpizza.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Setup beans required for the Authorization Server to function.
 */
@Configuration(proxyBeanMethods = false)
public class ServerConfig {
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		return http.build();
	}

	/**
	 * In this demo, the registered clients uses CLIENT_CREDENTIALS, with ID and
	 * Secret hard-coded, stored in memory. In production, this should be delegated
	 * to an Identity and Access Management system. In addition, AUTHORIZATION_CODE
	 * shall be used instead of CLIENT_CREDENTIALS as customers ordering pizza
	 * should be human, instead of servers.
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		final RegisteredClient registeredCustomer1 = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("pz-customer-1").clientSecret("my-top-secret1")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN).scope(OidcScopes.OPENID)
				.scope("orders.create").build();

		final RegisteredClient registeredAdmin2 = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("pz-admin-2").clientSecret("my-super-power2")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN).scope(OidcScopes.OPENID)
				.scope("orders.create").scope("orders.read").build();

		return new InMemoryRegisteredClientRepository(registeredCustomer1, registeredAdmin2);
	}

	/**
	 * For this demo, the JWKS will be re-generated every time this server restarts.
	 */
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		final RSAKey rsaKey = generateRsa();
		final JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	private static RSAKey generateRsa() {
		final KeyPair keyPair = generateRsaKey();
		final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		final RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
	}

	private static KeyPair generateRsaKey() {
		final KeyPair keyPair;
		try {
			final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public ProviderSettings providerSettings() {
		return ProviderSettings.builder().issuer("http://pizza-auth-server:8080").build();
	}

	/**
	 * The PasswordEncoder in this demo has no real use. We are just using
	 * CLIENT_CREDENTIALS.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
