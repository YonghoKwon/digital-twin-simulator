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
 * repeatBoolean : repeat or not
 * repeatTime : total duration in milliseconds when messageCount is not specified
 * messageCount : explicit number of generated messages. Takes precedence over repeatTime.
 *
 * filePath : tc format & data file Path
 * fileName : tc format & data file Name
 *
 * concurrentTasks : Number of concurrent tasks for load testing
 * </pre></blockquote>
 */
@ToString
@Getter
@Setter
public class ActiveMQRequestFileDto {
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

    private boolean repeatBoolean;

    @Min(0)
    private int repeatTime;

    @Min(0)
    private int messageCount;

    @NotBlank
    private String filePath;

    @NotBlank
    private String fileName;

    @Min(1)
    @Max(2000)
    private int concurrentTasks = 1;
}
