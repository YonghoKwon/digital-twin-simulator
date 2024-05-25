package com.dt.digitaltwinsimulator.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ActiveMQTaskInfoDto {
    private String taskId;
    private String taskCancelApiUrl;

    public ActiveMQTaskInfoDto(String taskId, String taskCancelApiUrl) {
        this.taskId = taskId;
        this.taskCancelApiUrl = taskCancelApiUrl;
    }
}
