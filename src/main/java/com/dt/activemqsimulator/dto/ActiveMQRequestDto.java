package com.dt.activemqsimulator.dto;

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
        activeMQ ip 주소 & 포트 :
        id :
        pw :
        topic :
        TC Name :
        반복유무 :
        반복시간 : (유 일 때)
        딜레이 타임 :
        포멧 : {
            항목1 : {
                타입 :
                랜덤유무 :
                랜덤범위 : (글자수 or 범위 등...)
            },
            항목2 : {

            }
        }
        값 : [
        ]
    }*/
    private String activeMQIp;
    private String id;
    private String pw;
    private String topic;
    private String tcName;
    private boolean repeat;
    private int repeatTime;
    private int delayTime;
//    private Map<String, Map<String, Object>> format;
    private Map<String, ActiveMQFormatDto> format;
    private List<Map<Integer, List<String>>> value;

}
