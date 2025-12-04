package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.model.Task;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkSessionServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Should start work session for task")
    void testStartWorkSession() {
        // Given
        taskService.addTask("Test task");
        Task task = taskService.getCurrentTask();

        // When
        workSessionService.startWorkSession(task.getId());

        // Then
        assertTrue(workSessionService.hasActiveWorkSession(task.getId()));
        assertNotNull(workSessionService.getActiveWorkSession(task.getId()));
    }

    @Test
    @DisplayName("Should pause active work session")
    void testPauseWorkSession() {
        // Given
        taskService.addTask("Test task");
        Task task = taskService.getCurrentTask();
        workSessionService.startWorkSession(task.getId());

        // When
        workSessionService.pauseWorkSession(task.getId());

        // Then
        assertFalse(workSessionService.hasActiveWorkSession(task.getId()));
        assertNull(workSessionService.getActiveWorkSession(task.getId()));
    }

    @Test
    @DisplayName("Should toggle work session from inactive to active")
    void testToggleWorkSessionStartsSession() {
        // Given
        taskService.addTask("Test task");
        Task task = taskService.getCurrentTask();

        // When
        workSessionService.toggleWorkSession(task.getId());

        // Then
        assertTrue(workSessionService.hasActiveWorkSession(task.getId()));
    }

    @Test
    @DisplayName("Should toggle work session from active to paused")
    void testToggleWorkSessionPausesSession() {
        // Given
        taskService.addTask("Test task");
        Task task = taskService.getCurrentTask();
        workSessionService.startWorkSession(task.getId());

        // When
        workSessionService.toggleWorkSession(task.getId());

        // Then
        assertFalse(workSessionService.hasActiveWorkSession(task.getId()));
    }

    @Test
    @DisplayName("Should format time correctly")
    void testFormatTime() {
        assertEquals("0h 0m", workSessionService.formatTime(0));
        assertEquals("0h 1m", workSessionService.formatTime(60));
        assertEquals("1h 0m", workSessionService.formatTime(3600));
        assertEquals("1h 30m", workSessionService.formatTime(5400));
        assertEquals("2h 15m", workSessionService.formatTime(8100));
    }

    @Test
    @DisplayName("Should return zero time for task with no sessions")
    void testGetTimeForTaskWithNoSessions() {
        // Given
        taskService.addTask("Test task");
        Task task = taskService.getCurrentTask();

        // When/Then
        assertEquals(0, workSessionService.getDailyTimeSeconds(task.getId()));
        assertEquals(0, workSessionService.getTotalTimeSeconds(task.getId()));
    }
}
