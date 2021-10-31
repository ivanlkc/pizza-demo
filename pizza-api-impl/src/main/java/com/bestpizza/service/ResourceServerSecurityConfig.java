package com.bestpizza.service;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Secure all endpoints with Oauth2. JWT must be obtained from the authorization
 * server. Roles are determined by token scope and applied on each method using
 * PreAuthorize annotations.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerSecurityConfig {
	/**
	 * Exclude actuator from the need of authentication, because it is used for K8S
	 * probes.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/actuator/**").permitAll().anyRequest().authenticated().and()
				.oauth2ResourceServer().jwt();
		return http.build();
	}

	/**
	 * To be able to decode JWTs, this service must access the JWK Set Uri of the
	 * authorization server.
	 */
	@Bean
	JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri()).build();
		return jwtDecoder;
	}
}
