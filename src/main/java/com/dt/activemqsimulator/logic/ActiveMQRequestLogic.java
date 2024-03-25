package com.dt.activemqsimulator.logic;

import com.dt.activemqsimulator.dto.ActiveMQRequestDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ActiveMQRequestLogic {

    @Async
    public void sendTopic(String flag, ActiveMQRequestDto activeMQRequestDto) {
        // activeMQ connection

        // message create

        // message send

        // connection close

        System.out.println("flag = " + flag + "activeMQRequestDto = " + activeMQRequestDto);
        // delay time 만큼 sleep
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("flag = " + flag + "activeMQRequestDto = " + activeMQRequestDto);
    }
}
