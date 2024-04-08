package com.dt.digitaltwinsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
public class ActiveMQRequestDto {
/*  {
        "activeMQ ip 주소 & 포트" :
        "id" :
        "pw" :
        "topic" :
        "TC Name" :
        "반복유무" :
        "반복시간" : (유 일 때)
        "딜레이 타임" :
        "포멧" : [
            {
                "항목id" :
                "항목타입" :
                "랜덤유무" : (true or false 일단은 개별 랜덤은 아직 안 함)
                "랜덤조건" : (범위...)
            }
        ]
        "값" : [
            {
                "0":["(,를 구분자로 하여 구분)"],
                "1":[]
            }
        ]
    }*/
    // 필수 항목들
    private String activeMQIp;
    private String id;
    private String pw;
    private String topic;
    private String tcName;
    private int delayTime;

    private boolean repeatBoolean;
    private int repeatTime;
//    private Map<String, Map<String, Object>> format;
//    private Map<String, ActiveMQFormatDto> format;
    private List<Map<String, String>> format;
    private List<Map<Integer, String>> value;

}
