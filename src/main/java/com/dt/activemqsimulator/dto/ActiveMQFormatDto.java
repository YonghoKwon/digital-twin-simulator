package com.dt.activemqsimulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ActiveMQFormatDto {
    /*항목1 : {
        타입 :
        랜덤유무 :
        랜덤범위 : (글자수 or 범위 등...)
    }*/
    private String type;
    private boolean random;
    private String randomRange;
}
