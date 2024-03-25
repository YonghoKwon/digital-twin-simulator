package com.dt.activemqsimulator.logic;

import com.dt.activemqsimulator.dto.ActiveMQRequestDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ActiveMQRequestLogic {

    @Async
    public CompletableFuture<String> sendTopic(String flag, ActiveMQRequestDto activeMQRequestDto) {
        return CompletableFuture.supplyAsync(() -> {

            // activeMQ connection

            // message create

            // message send

            // connection close

            System.out.println("start flag = " + flag + " activeMQRequestDto = " + activeMQRequestDto);
            for(int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("flag = " + flag + " i = " + i);
            }

            // delay time 만큼 sleep
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("end flag = " + flag + " activeMQRequestDto = " + activeMQRequestDto);

            return "success";
        });
    }
}
