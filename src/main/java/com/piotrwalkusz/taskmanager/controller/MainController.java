package com.piotrwalkusz.taskmanager.controller;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import com.piotrwalkusz.taskmanager.model.Task;
import com.piotrwalkusz.taskmanager.model.WorkSession;
import com.piotrwalkusz.taskmanager.service.TaskService;
import com.piotrwalkusz.taskmanager.service.WorkSessionService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {

    @FXML
    private TextField currentTaskLabel;

    @FXML
    private TextField timeLabel;

    @FXML
    private Button startPauseButton;

    @FXML
    private Button nextTaskButton;

    @FXML
    private TextField queueSizeLabel;

    @FXML
    private TextField newTaskTextField;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button deleteTaskButton;

    @FXML
    private Button undoButton;

    @FXML
    private TextField taskNameEditField;

    @FXML
    private VBox rootPane;

    private final DatabaseConfig databaseConfig = new DatabaseConfig();
    private final TaskService taskService = new TaskService(databaseConfig);
    private final WorkSessionService workSessionService = new WorkSessionService(databaseConfig);

    private Task currentTask;
    private Long lastCurrentTaskId; // Track when current task changes
    private WorkSession activeWorkSession; // Active session in memory only (not saved to DB until paused)
    private List<WorkSession> currentTaskSessions = new ArrayList<>(); // All sessions since task was displayed
    private long completedDailySeconds; // Time from DB (completed sessions today)
    private long completedTotalSeconds; // Time from DB (all completed sessions)
    private Timeline timeUpdateTimeline;

    @FXML
    public void initialize() {

        // Setup time update timeline (runs every 100ms for smooth updates)
        timeUpdateTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> updateTimeDisplay()));
        timeUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        timeUpdateTimeline.play();

        // Setup double-click handler for task name editing
        currentTaskLabel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && currentTask != null) {
                startEditingTaskName();
            }
        });

        // Setup TextField handlers
        taskNameEditField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveTaskName();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEditingTaskName();
            }
        });

        taskNameEditField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                saveTaskName();
            }
        });

        // Handle clicks anywhere to close edit mode (except on the TextField itself)
        rootPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            if (taskNameEditField.isVisible() && !isDescendant(taskNameEditField, (Node) event.getTarget())) {
                saveTaskName();
                event.consume();
            }
            // Remove focus from new task text field when clicking elsewhere
            if (newTaskTextField.isFocused() && !isDescendant(newTaskTextField, (Node) event.getTarget())) {
                rootPane.requestFocus();
            }
        });

        // Load initial state
        refreshUI();
    }

    @FXML
    private void handleAddTask() {
        String taskName = newTaskTextField.getText().trim();
        if (taskName.isEmpty()) {
            return;
        }

        taskService.addTask(taskName);
        newTaskTextField.clear();
        rootPane.requestFocus(); // Remove focus from text field
        refreshUI();
    }

    @FXML
    private void handleStartPause() {
        if (currentTask == null) {
            return;
        }

        // Cleanup deleted tasks before starting work
        taskService.cleanupDeletedTasks();

        if (activeWorkSession == null) {
            // Start new session in memory (not saved to DB yet)
            activeWorkSession = WorkSession.builder()
                    .taskId(currentTask.getId())
                    .startTime(Instant.now())
                    .build();
        } else {
            // Pause active session - set end time and save to DB
            activeWorkSession.setEndTime(Instant.now());
            workSessionService.saveWorkSession(activeWorkSession);
            currentTaskSessions.add(activeWorkSession);
            activeWorkSession = null;
        }

        refreshUI();
    }

    @FXML
    private void handleNextTask() {
        if (currentTask == null) {
            return;
        }

        // Save active session before rotating
        if (activeWorkSession != null) {
            activeWorkSession.setEndTime(Instant.now());
            workSessionService.saveWorkSession(activeWorkSession);
            currentTaskSessions.add(activeWorkSession);
            activeWorkSession = null;
        }

        // Cleanup deleted tasks before rotating
        taskService.cleanupDeletedTasks();

        // Rotate task
        taskService.rotateTask(currentTask.getId());

        refreshUI();
    }

    @FXML
    private void handleDeleteTask() {
        if (currentTask == null) {
            return;
        }

        // Save active session before deleting
        if (activeWorkSession != null) {
            activeWorkSession.setEndTime(Instant.now());
            workSessionService.saveWorkSession(activeWorkSession);
            currentTaskSessions.add(activeWorkSession);
            activeWorkSession = null;
        }

        // Cleanup old deleted tasks before deleting current one
        taskService.cleanupDeletedTasks();

        // Soft delete task
        taskService.softDeleteTask(currentTask.getId());

        refreshUI();
    }

    @FXML
    private void handleUndo() {
        // Restore deleted tasks
        taskService.undoDelete();

        refreshUI();
    }

    private void refreshUI() {
        // Load current task
        currentTask = taskService.getCurrentTask();

        // Detect task change - reset session list and load completed time from DB
        Long currentTaskId = currentTask != null ? currentTask.getId() : null;
        if (!Objects.equals(lastCurrentTaskId, currentTaskId)) {
            currentTaskSessions.clear();
            lastCurrentTaskId = currentTaskId;

            // Load work session state from database (completed sessions only) - ONLY when task changes
            if (currentTask != null) {
                completedDailySeconds = workSessionService.getDailyTimeSeconds(currentTask.getId());
                completedTotalSeconds = workSessionService.getTotalTimeSeconds(currentTask.getId());
            } else {
                completedDailySeconds = 0;
                completedTotalSeconds = 0;
            }
        }

        // Update task name (only if changed to preserve text selection)
        String newTaskText = (currentTask != null) ? currentTask.getName() : "No tasks in queue";
        if (!currentTaskLabel.getText().equals(newTaskText)) {
            currentTaskLabel.setText(newTaskText);
        }

        // Update buttons state
        updateButtonsState();

        // Update undo button visibility
        updateUndoButton();

        // Update time display
        updateTimeDisplay();

        // Update queue size (only if changed to preserve text selection)
        int queueSize = taskService.getQueueSize();
        String newQueueText = "Tasks: " + queueSize;
        if (!queueSizeLabel.getText().equals(newQueueText)) {
            queueSizeLabel.setText(newQueueText);
        }
    }

    private void updateButtonsState() {
        boolean hasTask = currentTask != null;
        boolean isActive = activeWorkSession != null;

        // Start/Pause button (icon changes)
        startPauseButton.setDisable(!hasTask);
        startPauseButton.setText(isActive ? "⏸" : "▶");

        // Next Task button
        nextTaskButton.setDisable(!hasTask);

        // Delete Task button
        deleteTaskButton.setDisable(!hasTask);
    }

    private void updateUndoButton() {
        boolean hasDeleted = taskService.hasDeletedTask();
        undoButton.setVisible(hasDeleted);
        undoButton.setManaged(hasDeleted);
    }

    private void updateTimeDisplay() {
        String newText;
        if (currentTask == null) {
            newText = "";
        } else {
            // Calculate time from current task sessions (since task was displayed)
            long currentTaskSeconds = 0;
            for (WorkSession session : currentTaskSessions) {
                long sessionSeconds = java.time.Duration.between(
                    session.getStartTime(),
                    session.getEndTime()
                ).getSeconds();
                currentTaskSeconds += sessionSeconds;
            }

            // Add active session time if any
            if (activeWorkSession != null) {
                long activeSessionSeconds = java.time.Duration.between(
                    activeWorkSession.getStartTime(),
                    Instant.now()
                ).getSeconds();
                currentTaskSeconds += activeSessionSeconds;
            }

            // Calculate total time: completed sessions from DB + current task sessions
            long dailySeconds = completedDailySeconds + currentTaskSeconds;
            long totalSeconds = completedTotalSeconds + currentTaskSeconds;

            String currentTime = workSessionService.formatTimeWithSeconds(currentTaskSeconds);
            String todayTime = workSessionService.formatTime(dailySeconds);
            String totalTime = workSessionService.formatTime(totalSeconds);

            newText = String.format("Time: %s (Today: %s, Total: %s)", currentTime, todayTime, totalTime);
        }

        // Only update if text actually changed to preserve text selection
        if (!timeLabel.getText().equals(newText)) {
            timeLabel.setText(newText);
        }
    }

    private void startEditingTaskName() {
        // Switch to edit mode
        currentTaskLabel.setVisible(false);
        currentTaskLabel.setManaged(false);
        taskNameEditField.setVisible(true);
        taskNameEditField.setManaged(true);

        // Set current task name in edit field
        taskNameEditField.setText(currentTask.getName());

        // Select all text and focus
        taskNameEditField.selectAll();
        taskNameEditField.requestFocus();
    }

    private void saveTaskName() {
        if (!taskNameEditField.isVisible() || currentTask == null) {
            return;
        }

        String newName = taskNameEditField.getText().trim();
        if (!newName.isEmpty() && !newName.equals(currentTask.getName())) {
            // Update task name in database
            taskService.updateTaskName(currentTask.getId(), newName);
        }

        // Switch back to display mode
        cancelEditingTaskName();

        // Refresh UI to show updated name
        refreshUI();
    }

    private void cancelEditingTaskName() {
        // Switch back to display mode
        taskNameEditField.setVisible(false);
        taskNameEditField.setManaged(false);
        currentTaskLabel.setVisible(true);
        currentTaskLabel.setManaged(true);
    }

    private boolean isDescendant(Node parent, Node node) {
        if (parent == node) {
            return true;
        }
        Node current = node;
        while (current != null) {
            if (current == parent) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public void onApplicationClose() {
        // Save active session before closing application
        if (activeWorkSession != null) {
            activeWorkSession.setEndTime(Instant.now());
            workSessionService.saveWorkSession(activeWorkSession);
        }
        // Stop the timeline to allow clean shutdown
        timeUpdateTimeline.stop();
    }
}
