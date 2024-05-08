package com.dt.digitaltwinsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ActiveMQ Request File Dto
 *
 * <blockquote><pre>
 * activeMQIp : activeMQ ip
 * id : activeMQ id
 * pw : activeMQ password
 * topic : activeMQ topic
 * tcName : transaction name
 *
 * delayTime : The interval between the previous message and the next message
 *
 * filePath : tc format & data file Path
 * formatFileName : tc format file Name
 * dataFileName : tc data file Name
 * </pre></blockquote>
 */
@ToString
@Getter
@Setter
public class ActiveMQRequestFileAndDataDto {
    private String activeMQIp;
    private String id;
    private String pw;
    private String topic;
    private String tcName;

    private int delayTime;

    private String filePath;
    private String formatFileName;
    private String dataFileName;
}
