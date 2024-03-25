package com.dt.activemqsimulator.controller;

import com.dt.activemqsimulator.dto.ActiveMQRequestDto;
import com.dt.activemqsimulator.logic.ActiveMQRequestLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class ActiveMQRequestController {

    private final ActiveMQRequestLogic activeMQRequestLogic;

    @Autowired
    public ActiveMQRequestController(ActiveMQRequestLogic activeMQRequestLogic) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @PostMapping("/activemq-normal")
    public String activemqNormal(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
        // get now time
        long now = System.currentTimeMillis();
        String flag = "activemq-normal" + now;
        activeMQRequestLogic.sendTopic(flag, activeMQRequestDto);

        return "success";
    }

    @PostMapping("/activemq")
    public CompletableFuture<String> activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
        // get now time
        long now = System.currentTimeMillis();
        String flag = "activemq" + now;

        return activeMQRequestLogic.sendTopic(flag, activeMQRequestDto)
                .thenApply(result -> "async success");
    }
}
