package com.example.r2dbcspringdatademo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

@SpringBootTest
@Testcontainers
class R2dbcSpringDataDemoApplicationTests {

	@Container
	static MariaDBContainer mariadb = new MariaDBContainer("mariadb:12.2-ubi");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", () -> "r2dbc:mariadb://" + mariadb.getHost() + ":" + mariadb.getFirstMappedPort() + "/" + mariadb.getDatabaseName());
		registry.add("spring.r2dbc.username", mariadb::getUsername);
		registry.add("spring.r2dbc.password", mariadb::getPassword);
	}

	@Test
	void contextLoads() {
	}

}
