package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.model.Task;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkSessionServiceTest extends BaseServiceTest {

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
