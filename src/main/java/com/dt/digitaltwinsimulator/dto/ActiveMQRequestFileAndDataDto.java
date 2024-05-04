package com.dt.digitaltwinsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ActiveMQRequestFileAndDataDto {
    // 필수 항목들
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
