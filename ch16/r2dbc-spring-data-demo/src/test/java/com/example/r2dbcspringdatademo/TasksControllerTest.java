package com.example.r2dbcspringdatademo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasksControllerTest {

    @Mock
    private TaskService taskService;

    private TasksController controller;

    @BeforeEach
    void setUp() {
        controller = new TasksController();
        ReflectionTestUtils.setField(controller, "service", taskService);
    }

    @Test
    void get_shouldReturnAllTasks() {
        Task task1 = new Task("Task 1");
        task1.setId(1);
        Task task2 = new Task("Task 2");
        task2.setId(2);

        when(taskService.getAllTasks()).thenReturn(Flux.just(task1, task2));

        ResponseEntity<Flux<Task>> response = controller.get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StepVerifier.create(response.getBody())
                .expectNext(task1)
                .expectNext(task2)
                .verifyComplete();
    }

    @Test
    void post_shouldCreateTask_whenValid() {
        Task task = new Task("New task");
        Task saved = new Task("New task");
        saved.setId(1);

        when(taskService.isValid(any(Task.class))).thenReturn(true);
        when(taskService.createTask(any(Task.class))).thenReturn(Mono.just(saved));

        ResponseEntity<Mono<Task>> response = controller.post(task);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StepVerifier.create(response.getBody())
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void post_shouldReturnTeapot_whenInvalid() {
        Task task = new Task("");

        when(taskService.isValid(any(Task.class))).thenReturn(false);

        ResponseEntity<Mono<Task>> response = controller.post(task);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        verify(taskService, never()).createTask(any());
    }

    @Test
    void put_shouldUpdateTask_whenValid() {
        Task task = new Task("Updated");
        task.setId(1);
        task.setCompleted(true);

        when(taskService.isValid(any(Task.class))).thenReturn(true);
        when(taskService.updateTask(any(Task.class))).thenReturn(Mono.just(task));

        ResponseEntity<Mono<Task>> response = controller.put(task);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StepVerifier.create(response.getBody())
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void put_shouldReturnTeapot_whenInvalid() {
        Task task = new Task("");

        when(taskService.isValid(any(Task.class))).thenReturn(false);

        ResponseEntity<Mono<Task>> response = controller.put(task);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        verify(taskService, never()).updateTask(any());
    }

    @Test
    void updateStatus_shouldUpdate_whenIdValid() {
        when(taskService.updateTaskStatusById(1, true)).thenReturn(Mono.just(1));

        ResponseEntity<Mono<Integer>> response = controller.updateStatus(1, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StepVerifier.create(response.getBody())
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void updateStatus_shouldReturnTeapot_whenIdInvalid() {
        ResponseEntity<Mono<Integer>> response = controller.updateStatus(0, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        verify(taskService, never()).updateTaskStatusById(anyInt(), anyBoolean());
    }

    @Test
    void delete_shouldDelete_whenIdValid() {
        when(taskService.deleteTask(1)).thenReturn(Mono.empty());

        ResponseEntity<Mono<Void>> response = controller.delete(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StepVerifier.create(response.getBody())
                .verifyComplete();
    }

    @Test
    void delete_shouldReturnTeapot_whenIdInvalid() {
        ResponseEntity<Mono<Void>> response = controller.delete(0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        verify(taskService, never()).deleteTask(anyInt());
    }
}
