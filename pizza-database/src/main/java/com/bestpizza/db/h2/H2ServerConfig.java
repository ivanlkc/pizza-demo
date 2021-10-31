package com.bestpizza.db.h2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and starts the H2 server.
 */
@Configuration
@ConfigurationProperties("pizza.database")
@lombok.extern.slf4j.Slf4j
public class H2ServerConfig {
	@Autowired
	private DataSource dataSource;

	@lombok.Getter
	@lombok.Setter
	private int tcpPort;

	@lombok.Getter
	@lombok.Setter
	private int webPort;

	/**
	 * Starts H2 TCP Server which allows JDBC connections.
	 */
	@Bean(destroyMethod = "stop")
	public Server h2tcpServer() throws SQLException {
		final Server result = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort",
				String.valueOf(this.tcpPort));
		log.info("H2 TCP Server configured on port {}", this.tcpPort);
		result.start();
		// The H2 server is still empty after it started.
		// A local connection must be made to trigger the creation of the database.
		try (Connection c = dataSource.getConnection()) {
			log.info("H2 initial connection success! Connection class: {}", c.getClass());
		}
		return result;
	}

	/**
	 * Starts H2 WEB Server which allows troubleshooting by opening the h2-console
	 * in a browser.
	 */
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2webServer() throws SQLException {
		final Server result = Server.createWebServer("-web", "-webAllowOthers", "-webPort",
				String.valueOf(this.webPort));
		log.info("H2 WEB Server configured on port {}", this.webPort);
		return result;
	}
}
