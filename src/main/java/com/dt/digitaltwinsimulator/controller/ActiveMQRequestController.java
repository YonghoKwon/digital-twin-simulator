package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.logic.ActiveMQRequestLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ActiveMQRequestController {

    private final ActiveMQRequestLogic activeMQRequestLogic;

    @Autowired
    public ActiveMQRequestController(
            ActiveMQRequestLogic activeMQRequestLogic
    ) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @PostMapping("/activemq/{taskId}")
    public String activemqNormal(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestDto activeMQRequestDto
    ) {
        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생

        activeMQRequestLogic.sendTopic(taskId, activeMQRequestDto);
        return "success : taskId : " + taskId;
    }

    @PostMapping("/activemq")
//    public CompletableFuture<String> activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
    public String activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
        // get now time
        long now = System.currentTimeMillis();
        String flag = "activemq" + now;

        activeMQRequestLogic.sendTopic(flag, activeMQRequestDto);
        return "success";
//        return activeMQRequestLogic.sendTopic(flag, activeMQRequestDto)
//                .thenApply(result -> "async success");
    }
}
