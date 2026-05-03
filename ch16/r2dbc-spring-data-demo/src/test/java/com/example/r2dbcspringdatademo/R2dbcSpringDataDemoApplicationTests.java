package com.example.r2dbcspringdatademo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

	@LocalServerPort
	private int port;

	private WebTestClient webTestClient;

	@Autowired
	private DatabaseClient databaseClient;

	@Autowired
	private TasksRepository tasksRepository;

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				.build();
		// Create table and clean data before each test
		databaseClient.sql("CREATE TABLE IF NOT EXISTS tasks (id INT AUTO_INCREMENT PRIMARY KEY, description VARCHAR(255) NOT NULL, completed BOOLEAN DEFAULT FALSE)")
				.then().block();
		databaseClient.sql("DELETE FROM tasks").then().block();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateTask() {
		webTestClient.post().uri("/tasks")
				.bodyValue(new Task("Test task"))
				.exchange()
				.expectStatus().isOk()
				.expectBody(Task.class)
				.value(task -> {
					assertThat(task.getId()).isNotNull();
					assertThat(task.getDescription()).isEqualTo("Test task");
				});
	}

	@Test
	void shouldGetAllTasks() {
		// Insert test data
		tasksRepository.save(new Task("Task 1")).block();
		tasksRepository.save(new Task("Task 2")).block();

		webTestClient.get().uri("/tasks")
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Task.class)
				.hasSize(2);
	}

	@Test
	void shouldUpdateTask() {
		Task saved = tasksRepository.save(new Task("Original")).block();

		Task updated = new Task("Updated");
		updated.setId(saved.getId());
		updated.setCompleted(true);

		webTestClient.put().uri("/tasks")
				.bodyValue(updated)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Task.class)
				.value(task -> {
					assertThat(task.getDescription()).isEqualTo("Updated");
					assertThat(task.getCompleted()).isTrue();
				});
	}

	@Test
	void shouldUpdateTaskStatus() {
		Task saved = tasksRepository.save(new Task("Status task")).block();

		webTestClient.put().uri("/tasks/updatestatus?id={id}&completed=true", saved.getId())
				.exchange()
				.expectStatus().isOk();

		// Verify the status was updated
		StepVerifier.create(tasksRepository.findById(saved.getId()))
				.assertNext(task -> assertThat(task.getCompleted()).isTrue())
				.verifyComplete();
	}

	@Test
	void shouldDeleteTask() {
		Task saved = tasksRepository.save(new Task("To delete")).block();

		webTestClient.delete().uri("/tasks?id={id}", saved.getId())
				.exchange()
				.expectStatus().isOk();

		// Verify task was deleted
		StepVerifier.create(tasksRepository.findById(saved.getId()))
				.verifyComplete();
	}

	@Test
	void shouldReturnTeapotForInvalidPost() {
		Task invalid = new Task("");

		webTestClient.post().uri("/tasks")
				.bodyValue(invalid)
				.exchange()
				.expectStatus().isEqualTo(418);
	}

	@Test
	void shouldReturnTeapotForInvalidDeleteId() {
		webTestClient.delete().uri("/tasks?id=0")
				.exchange()
				.expectStatus().isEqualTo(418);
	}

	@Test
	void shouldRepositoryUpdateStatus() {
		Task saved = tasksRepository.save(new Task("Repo test")).block();

		StepVerifier.create(tasksRepository.updateStatus(saved.getId(), true))
				.expectNext(1)
				.verifyComplete();

		StepVerifier.create(tasksRepository.findById(saved.getId()))
				.assertNext(task -> assertThat(task.getCompleted()).isTrue())
				.verifyComplete();
	}
}
