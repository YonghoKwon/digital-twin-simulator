package com.dt.digitaltwinsimulator.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ActiveMQ Request File Dto
 *
 * <blockquote><pre>
 * activeMQIp : ActiveMQ ip
 * id : ActiveMQ id
 * pw : ActiveMQ password
 * topic : ActiveMQ topic
 * tcName : transaction name
 *
 * delayTime : The interval between the previous message and the next message
 *
 * filePath : tc format & data file Path
 * formatFileName : tc format file Name
 * dataFileName : tc data file Name
 *
 * concurrentTasks : Number of concurrent tasks for load testing
 * </pre></blockquote>
 */
@ToString
@Getter
@Setter
public class ActiveMQRequestFileAndDataDto {
    @NotBlank
    private String activeMQIp;

    @NotBlank
    private String id;

    @NotBlank
    private String pw;

    @NotBlank
    private String topic;

    @NotBlank
    private String tcName;

    @Min(0)
    private int delayTime;

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
