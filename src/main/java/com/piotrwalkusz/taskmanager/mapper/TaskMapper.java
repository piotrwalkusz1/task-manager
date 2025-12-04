package com.piotrwalkusz.taskmanager.mapper;

import com.piotrwalkusz.taskmanager.model.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    /**
     * Get current task (head of queue)
     */
    Task getCurrentTask();

    /**
     * Add new task to end of queue
     */
    void insertTask(Task task);

    /**
     * Move task to end of queue
     */
    void rotateTask(@Param("taskId") Long taskId);

    /**
     * Get total number of tasks in queue
     */
    int getQueueSize();

    /**
     * Get all tasks ordered by queue_order
     */
    List<Task> getAllTasks();

    /**
     * Get max queue_order value
     */
    Integer getMaxQueueOrder();
}
