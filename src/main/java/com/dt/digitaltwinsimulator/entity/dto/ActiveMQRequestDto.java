package com.dt.digitaltwinsimulator.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ActiveMQ Request(Normal) Dto
 *
 * <blockquote><pre>
 * activeMQIp : activeMQ ip
 * id : ActiveMQ id
 * pw : ActiveMQ password
 * topic : ActiveMQ topic
 * tcName : transaction name
 *
 * delayTime : The interval between the previous message and the next message
 * repeatBoolean : repeat or not
 * repeatTime : total duration in milliseconds when messageCount is not specified
 * messageCount : explicit number of generated message batches. Takes precedence over repeatTime.
 *
 * format : tc format
 * value : tc data. Empty value means random generation from format.
 *
 * concurrentTasks : Number of concurrent tasks for load testing
 * </pre></blockquote>
 */
@ToString
@Getter
@Setter
public class ActiveMQRequestDto {
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

    /**
     * Explicit generated message batch count.
     * <p>For random generation this is the total message count.</p>
     * <p>For value-based generation this is the repeat count over the supplied value rows.</p>
     */
    @Min(0)
    private int messageCount;

    @NotEmpty
    private List<Map<String, String>> format;

    private List<Map<Integer, String>> value;

    @Min(1)
    @Max(2000)
    private int concurrentTasks = 1;
}
