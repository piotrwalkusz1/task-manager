package com.piotrwalkusz.taskmanager.controller;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import com.piotrwalkusz.taskmanager.model.Task;
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
    private Timeline timeUpdateTimeline;

    @FXML
    public void initialize() {

        // Setup time update timeline (runs every second)
        timeUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeDisplay()));
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
        String newText;
        if (currentTask == null) {
            newText = "";
        } else {
            long dailySeconds = workSessionService.getDailyTimeSeconds(currentTask.getId());
            long totalSeconds = workSessionService.getTotalTimeSeconds(currentTask.getId());

            String dailyTime = workSessionService.formatTimeWithSeconds(dailySeconds);
            String totalTime = workSessionService.formatTime(totalSeconds);

            newText = String.format("Time today: %s (Total: %s)", dailyTime, totalTime);
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
}
