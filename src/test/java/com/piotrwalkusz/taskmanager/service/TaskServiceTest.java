package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.model.Task;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Should add task to empty queue with queueOrder 1")
    void testAddTaskToEmptyQueue() {
        // When
        taskService.addTask("First task");

        // Then
        Task currentTask = taskService.getCurrentTask();
        assertNotNull(currentTask);
        assertEquals("First task", currentTask.getName());
        assertEquals(1, taskService.getQueueSize());
    }

    @Test
    @DisplayName("Should add multiple tasks and maintain queue order")
    void testAddMultipleTasks() {
        // When
        taskService.addTask("Task 1");
        taskService.addTask("Task 2");
        taskService.addTask("Task 3");

        // Then
        assertEquals(3, taskService.getQueueSize());
        Task currentTask = taskService.getCurrentTask();
        assertEquals("Task 1", currentTask.getName());
    }

    @Test
    @DisplayName("Should return null when getting current task from empty queue")
    void testGetCurrentTaskFromEmptyQueue() {
        // When
        Task currentTask = taskService.getCurrentTask();

        // Then
        assertNull(currentTask);
        assertEquals(0, taskService.getQueueSize());
    }

    @Test
    @DisplayName("Should rotate task to end of queue")
    void testRotateTask() {
        // Given
        taskService.addTask("Task 1");
        taskService.addTask("Task 2");
        taskService.addTask("Task 3");
        Task firstTask = taskService.getCurrentTask();

        // When
        taskService.rotateTaskWithPause(firstTask.getId());

        // Then
        Task newCurrentTask = taskService.getCurrentTask();
        assertEquals("Task 2", newCurrentTask.getName());
        assertEquals(3, taskService.getQueueSize());

        // Verify first task is now at the end
        var allTasks = taskService.getAllTasks();
        assertEquals("Task 1", allTasks.get(2).getName());
    }

    @Test
    @DisplayName("Should handle single task rotation")
    void testRotateSingleTask() {
        // Given
        taskService.addTask("Only task");
        Task task = taskService.getCurrentTask();

        // When
        taskService.rotateTaskWithPause(task.getId());

        // Then
        Task currentTask = taskService.getCurrentTask();
        assertEquals("Only task", currentTask.getName());
        assertEquals(1, taskService.getQueueSize());
    }

    @Test
    @DisplayName("Should pause active session when rotating task")
    void testRotateTaskPausesActiveSession() {
        // Given
        taskService.addTask("Task 1");
        taskService.addTask("Task 2");
        Task task1 = taskService.getCurrentTask();

        // Start work session
        workSessionService.startWorkSession(task1.getId());
        assertTrue(workSessionService.hasActiveWorkSession(task1.getId()));

        // When - rotate should pause active session
        taskService.rotateTaskWithPause(task1.getId());

        // Then
        assertFalse(workSessionService.hasActiveWorkSession(task1.getId()));
    }
}
