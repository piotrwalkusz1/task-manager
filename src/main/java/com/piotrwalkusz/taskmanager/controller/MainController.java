package com.piotrwalkusz.taskmanager.controller;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import com.piotrwalkusz.taskmanager.model.Task;
import com.piotrwalkusz.taskmanager.service.TaskService;
import com.piotrwalkusz.taskmanager.service.WorkSessionService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class MainController {

    @FXML
    private Label currentTaskLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Button startPauseButton;

    @FXML
    private Button nextTaskButton;

    @FXML
    private Label queueSizeLabel;

    @FXML
    private TextField newTaskTextField;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button deleteTaskButton;

    @FXML
    private Button undoButton;

    private final DatabaseConfig databaseConfig = new DatabaseConfig();
    private final TaskService taskService = new TaskService(databaseConfig);
    private final WorkSessionService workSessionService = new WorkSessionService(databaseConfig);

    private Task currentTask;
    private Timeline timeUpdateTimeline;

    @FXML
    public void initialize() {

        // Setup time update timeline (runs every second)
        timeUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeDisplay()));
        timeUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        timeUpdateTimeline.play();

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
        refreshUI();
    }

    @FXML
    private void handleStartPause() {
        if (currentTask == null) {
            return;
        }

        // Cleanup deleted tasks before starting work
        taskService.cleanupDeletedTasks();

        // Toggle work session (transactional)
        workSessionService.toggleWorkSession(currentTask.getId());

        refreshUI();
    }

    @FXML
    private void handleNextTask() {
        if (currentTask == null) {
            return;
        }

        // Cleanup deleted tasks before rotating
        taskService.cleanupDeletedTasks();

        // Pause if in progress and rotate task (transactional)
        taskService.rotateTaskWithPause(currentTask.getId());

        refreshUI();
    }

    @FXML
    private void handleDeleteTask() {
        if (currentTask == null) {
            return;
        }

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

        // Update task name
        if (currentTask != null) {
            currentTaskLabel.setText(currentTask.getName());
        } else {
            currentTaskLabel.setText("No tasks in queue");
        }

        // Update buttons state
        updateButtonsState();

        // Update undo button visibility
        updateUndoButton();

        // Update time display
        updateTimeDisplay();

        // Update queue size
        int queueSize = taskService.getQueueSize();
        queueSizeLabel.setText("Tasks: " + queueSize);
    }

    private void updateButtonsState() {
        boolean hasTask = currentTask != null;
        boolean isActive = hasTask && workSessionService.hasActiveWorkSession(currentTask.getId());

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
        if (currentTask == null) {
            timeLabel.setText("");
            return;
        }

        long dailySeconds = workSessionService.getDailyTimeSeconds(currentTask.getId());
        long totalSeconds = workSessionService.getTotalTimeSeconds(currentTask.getId());

        String dailyTime = workSessionService.formatTimeWithSeconds(dailySeconds);
        String totalTime = workSessionService.formatTime(totalSeconds);

        timeLabel.setText(String.format("Time today: %s (Total: %s)", dailyTime, totalTime));
    }
}
