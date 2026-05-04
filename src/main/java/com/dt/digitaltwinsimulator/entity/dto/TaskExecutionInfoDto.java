package com.dt.digitaltwinsimulator.entity.dto;

import com.dt.digitaltwinsimulator.entity.TaskExecutionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class TaskExecutionInfoDto {
    private String taskId;
    private TaskExecutionStatus status;
    private String taskCancelApiUrl;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private long sentCount;
    private String message;

    public TaskExecutionInfoDto(
            String taskId,
            TaskExecutionStatus status,
            String taskCancelApiUrl,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            long sentCount,
            String message
    ) {
        this.taskId = taskId;
        this.status = status;
        this.taskCancelApiUrl = taskCancelApiUrl;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.sentCount = sentCount;
        this.message = message;
    }
}
