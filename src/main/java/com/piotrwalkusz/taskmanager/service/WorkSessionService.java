package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import com.piotrwalkusz.taskmanager.mapper.WorkSessionMapper;
import com.piotrwalkusz.taskmanager.model.WorkSession;
import org.apache.ibatis.session.SqlSession;

import java.time.Instant;

/**
 * Service for managing work sessions
 */
public class WorkSessionService {

    private final DatabaseConfig databaseConfig;

    public WorkSessionService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Start new work session for task
     */
    public void startWorkSession(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            WorkSession workSession = WorkSession.builder()
                    .taskId(taskId)
                    .startTime(Instant.now())
                    .build();
            mapper.insertWorkSession(workSession);
            session.commit();
        }
    }

    /**
     * Pause active work session for task
     */
    public void pauseWorkSession(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            mapper.pauseWorkSession(taskId);
            session.commit();
        }
    }

    /**
     * Check if task has active work session
     */
    public boolean hasActiveWorkSession(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            return mapper.hasActiveWorkSession(taskId);
        }
    }

    /**
     * Get active work session for task
     */
    public WorkSession getActiveWorkSession(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            return mapper.getActiveWorkSession(taskId);
        }
    }

    /**
     * Get daily time spent on task (in seconds)
     */
    public long getDailyTimeSeconds(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            Long time = mapper.getDailyTimeSeconds(taskId);
            return time != null ? time : 0;
        }
    }

    /**
     * Get total time spent on task (in seconds)
     */
    public long getTotalTimeSeconds(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);
            Long time = mapper.getTotalTimeSeconds(taskId);
            return time != null ? time : 0;
        }
    }

    /**
     * Toggle work session - start if not active, pause if active (transactional)
     */
    public void toggleWorkSession(Long taskId) {
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            WorkSessionMapper mapper = session.getMapper(WorkSessionMapper.class);

            // Check and toggle in same transaction
            boolean hasActiveSession = mapper.hasActiveWorkSession(taskId);

            if (hasActiveSession) {
                // Pause active session
                mapper.pauseWorkSession(taskId);
            } else {
                // Start new session
                WorkSession workSession = WorkSession.builder()
                        .taskId(taskId)
                        .startTime(Instant.now())
                        .build();
                mapper.insertWorkSession(workSession);
            }

            session.commit();
        }
    }

    /**
     * Format seconds to human-readable time (Xh Ym)
     */
    public String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }

    /**
     * Format seconds to human-readable time with seconds (Xh Ym Zs)
     */
    public String formatTimeWithSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, secs);
    }
}
