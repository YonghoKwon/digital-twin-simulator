package com.dt.digitaltwinsimulator.dto;

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
 * id : activeMQ id
 * pw : activeMQ password
 * topic : activeMQ topic
 * tcName : transaction name
 *
 * delayTime : The interval between the previous message and the next message
 * repeatBoolean : repeat or not
 * repeatTime : the number of repetitions
 *
 * format : tc format
 * value : tc data
 * </pre></blockquote>
 */
@ToString
@Getter
@Setter
public class ActiveMQRequestDto {
    private String activeMQIp;
    private String id;
    private String pw;
    private String topic;
    private String tcName;

    private int delayTime;
    private boolean repeatBoolean;
    private int repeatTime;

    private List<Map<String, String>> format;
    private List<Map<Integer, String>> value;

}
