package com.dt.digitaltwinsimulator.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoadTestResultDto {
    private String taskId;
    private long requestedCount;
    private long successCount;
    private long failureCount;
    private long elapsedMillis;
    private double actualTps;
    private int workerCount;
    private int targetTps;
    private boolean cancelled;
    private String message;

    public LoadTestResultDto(
            String taskId,
            long requestedCount,
            long successCount,
            long failureCount,
            long elapsedMillis,
            double actualTps,
            int workerCount,
            int targetTps,
            boolean cancelled,
            String message
    ) {
        this.taskId = taskId;
        this.requestedCount = requestedCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.elapsedMillis = elapsedMillis;
        this.actualTps = actualTps;
        this.workerCount = workerCount;
        this.targetTps = targetTps;
        this.cancelled = cancelled;
        this.message = message;
    }
}
