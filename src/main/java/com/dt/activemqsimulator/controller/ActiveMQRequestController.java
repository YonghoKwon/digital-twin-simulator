package com.dt.activemqsimulator.controller;

import com.dt.activemqsimulator.dto.ActiveMQRequestDto;
import com.dt.activemqsimulator.logic.ActiveMQRequestLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ActiveMQRequestController {

    private final ActiveMQRequestLogic activeMQRequestLogic;

    @Autowired
    public ActiveMQRequestController(ActiveMQRequestLogic activeMQRequestLogic) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @PostMapping("/activemq")
    public String activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
        // get now time
        long now = System.currentTimeMillis();
        String flag = "activemq" + now;
        activeMQRequestLogic.sendTopic(flag, activeMQRequestDto);

        return "success";
    }

    @PostMapping("/activemq2")
    public String activemq2(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
        // get now time
        long now = System.currentTimeMillis();
        String flag = "activemq2" + now;
        activeMQRequestLogic.sendTopic(flag, activeMQRequestDto);

        return "success";
    }
}
