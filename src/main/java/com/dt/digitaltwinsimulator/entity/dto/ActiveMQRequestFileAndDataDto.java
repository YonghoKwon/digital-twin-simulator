package com.dt.digitaltwinsimulator.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ActiveMQRequestFileAndDataDto {
    private String activeMQIp;
    private String id;
    private String pw;
    private String topic;

    @NotBlank
    private String tcName;

    @Min(0)
    private int delayTime;

    @Min(0)
    @Max(1_000_000)
    private int targetTps;

    private boolean repeatBoolean;

    @Min(0)
    private int repeatTime;

    @Min(0)
    private int messageCount;

    @NotBlank
    private String filePath;

    @NotBlank
    private String formatFileName;

    @NotBlank
    private String dataFileName;

    @Min(1)
    @Max(2000)
    private int concurrentTasks = 1;
}
