package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import com.piotrwalkusz.taskmanager.mapper.TaskMapper;
import com.piotrwalkusz.taskmanager.mapper.WorkSessionMapper;
import com.piotrwalkusz.taskmanager.model.Task;
import org.apache.ibatis.session.SqlSession;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing tasks
 */
public class TaskService {

    private final DatabaseConfig databaseConfig;

    public TaskService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Get current task (head of queue)
     */
    public Task getCurrentTask() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            return mapper.getCurrentTask();
        }
    }

    /**
     * Add new task to end of queue
     */
    public void addTask(String name) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            Task task = Task.builder()
                    .name(name)
                    .createdAt(Instant.now())
                    .build();
            mapper.insertTask(task);
            session.commit();
        }
    }

    /**
     * Move task to end of queue (transactional)
     * If task has active work session, pause it first in the same transaction
     */
    public void rotateTaskWithPause(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper workSessionMapper = session.getMapper(WorkSessionMapper.class);
            TaskMapper taskMapper = session.getMapper(TaskMapper.class);

            // Check and pause active session in same transaction
            boolean hasActiveSession = workSessionMapper.hasActiveWorkSession(taskId);
            if (hasActiveSession) {
                workSessionMapper.pauseWorkSession(taskId);
            }

            // Rotate task
            taskMapper.rotateTask(taskId);

            session.commit();
        }
    }

    /**
     * Get total number of tasks in queue
     */
    public int getQueueSize() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            return mapper.getQueueSize();
        }
    }

    /**
     * Get all tasks ordered by queue_order
     */
    public List<Task> getAllTasks() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            return mapper.getAllTasks();
        }
    }

    /**
     * Check if there is any deleted task
     */
    public boolean hasDeletedTask() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            return mapper.hasDeletedTask();
        }
    }

    /**
     * Soft delete task by ID
     */
    public void softDeleteTask(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            mapper.softDeleteTask(taskId);
            session.commit();
        }
    }

    /**
     * Undo delete - restore all deleted tasks
     */
    public void undoDelete() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            mapper.undoDelete();
            session.commit();
        }
    }

    /**
     * Permanently delete all soft-deleted tasks
     */
    public void cleanupDeletedTasks() {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            mapper.cleanupDeletedTasks();
            session.commit();
        }
    }

    /**
     * Update task name
     */
    public void updateTaskName(Long taskId, String name) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            TaskMapper mapper = session.getMapper(TaskMapper.class);
            mapper.updateTaskName(taskId, name);
            session.commit();
        }
    }
}
