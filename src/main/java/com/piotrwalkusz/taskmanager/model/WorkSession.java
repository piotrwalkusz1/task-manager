package com.piotrwalkusz.taskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSession {
    private Long id;
    private Long taskId;
    private Instant startTime;
    private Instant endTime;
}
