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
public class Task {
    private Long id;
    private String name;
    private Integer queueOrder;
    private Instant createdAt;
    private Boolean isDeleted;
}
