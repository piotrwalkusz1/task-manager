package com.piotrwalkusz.taskmanager.mapper;

import com.piotrwalkusz.taskmanager.model.WorkSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkSessionMapper {

    /**
     * Start new work session for task
     */
    void insertWorkSession(WorkSession workSession);

    /**
     * Pause active work session for task
     */
    void pauseWorkSession(@Param("taskId") Long taskId);

    /**
     * Get active work session for task
     */
    WorkSession getActiveWorkSession(@Param("taskId") Long taskId);

    /**
     * Check if task has active work session
     */
    boolean hasActiveWorkSession(@Param("taskId") Long taskId);

    /**
     * Get daily time spent on task (in seconds)
     */
    Long getDailyTimeSeconds(@Param("taskId") Long taskId);

    /**
     * Get total time spent on task (in seconds)
     */
    Long getTotalTimeSeconds(@Param("taskId") Long taskId);
}
