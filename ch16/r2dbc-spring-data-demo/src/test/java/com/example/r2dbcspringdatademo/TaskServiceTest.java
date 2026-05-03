package com.example.r2dbcspringdatademo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TasksRepository repository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Sample task");
        sampleTask.setId(1);
        sampleTask.setCompleted(false);
    }

    @Test
    void isValid_shouldReturnTrue_whenTaskIsValid() {
        Task task = new Task("Valid description");
        assertThat(taskService.isValid(task)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenTaskIsNull() {
        assertThat(taskService.isValid(null)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenDescriptionIsEmpty() {
        Task task = new Task("");
        assertThat(taskService.isValid(task)).isFalse();
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task2 = new Task("Another task");
        task2.setId(2);
        when(repository.findAll()).thenReturn(Flux.just(sampleTask, task2));

        StepVerifier.create(taskService.getAllTasks())
                .expectNext(sampleTask)
                .expectNext(task2)
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void getAllTasks_shouldReturnEmpty_whenNoTasks() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(taskService.getAllTasks())
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void createTask_shouldSaveAndReturnTask() {
        when(repository.save(any(Task.class))).thenReturn(Mono.just(sampleTask));

        StepVerifier.create(taskService.createTask(new Task("Sample task")))
                .assertNext(task -> {
                    assertThat(task.getId()).isEqualTo(1);
                    assertThat(task.getDescription()).isEqualTo("Sample task");
                })
                .verifyComplete();

        verify(repository).save(any(Task.class));
    }

    @Test
    void updateTask_shouldUpdateExistingTask() {
        Task updatedTask = new Task("Updated description");
        updatedTask.setId(1);
        updatedTask.setCompleted(true);

        when(repository.findById(1)).thenReturn(Mono.just(sampleTask));
        when(repository.save(any(Task.class))).thenReturn(Mono.just(updatedTask));

        StepVerifier.create(taskService.updateTask(updatedTask))
                .assertNext(task -> {
                    assertThat(task.getDescription()).isEqualTo("Updated description");
                    assertThat(task.getCompleted()).isTrue();
                })
                .verifyComplete();

        verify(repository).findById(1);
        verify(repository).save(any(Task.class));
    }

    @Test
    void updateTask_shouldReturnEmpty_whenTaskNotFound() {
        Task updatedTask = new Task("Not found");
        updatedTask.setId(999);

        when(repository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.updateTask(updatedTask))
                .verifyComplete();

        verify(repository).findById(999);
        verify(repository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatusById_shouldReturnUpdateCount() {
        when(repository.updateStatus(1, true)).thenReturn(Mono.just(1));

        StepVerifier.create(taskService.updateTaskStatusById(1, true))
                .expectNext(1)
                .verifyComplete();

        verify(repository).updateStatus(1, true);
    }

    @Test
    void deleteTask_shouldDeleteExistingTask() {
        when(repository.findById(1)).thenReturn(Mono.just(sampleTask));
        when(repository.delete(sampleTask)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.deleteTask(1))
                .verifyComplete();

        verify(repository).findById(1);
        verify(repository).delete(sampleTask);
    }

    @Test
    void deleteTask_shouldDoNothing_whenTaskNotFound() {
        when(repository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(taskService.deleteTask(999))
                .verifyComplete();

        verify(repository).findById(999);
        verify(repository, never()).delete(any(Task.class));
    }
}
