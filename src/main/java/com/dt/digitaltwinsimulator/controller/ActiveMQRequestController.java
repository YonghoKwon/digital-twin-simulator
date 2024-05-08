package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.dto.ActiveMQRequestFileAndDataDto;
import com.dt.digitaltwinsimulator.dto.ActiveMQRequestFileDto;
import com.dt.digitaltwinsimulator.logic.ActiveMQRequestLogic;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "ActiveMQ Request Controller")
@RestController
public class ActiveMQRequestController {

    private final ActiveMQRequestLogic activeMQRequestLogic;

    @Autowired
    public ActiveMQRequestController(
            ActiveMQRequestLogic activeMQRequestLogic
    ) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @ApiOperation(value = "ActiveMQ 메시지 전송", notes = "ActiveMQ message send")
    @PostMapping("/activemq/{taskId}")
    public String activemqNormal(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestDto activeMQRequestDto
    ) {
        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생

        activeMQRequestLogic.sendTopic(taskId, activeMQRequestDto);
        return "success : taskId : " + taskId;
    }

    @ApiOperation(value = "ActiveMQ 파일 메시지 전송(동일한 메시지 반복)", notes = "ActiveMQ file message send")
    @PostMapping("/activemq/file/{taskId}")
    public String activemqFile(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestFileDto activeMQRequestFileDto
    ) {
        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생성

        activeMQRequestLogic.sendFileTopic(taskId, activeMQRequestFileDto);
        return "success : taskId : " + taskId;
    }

    @ApiOperation(value = "ActiveMQ 파일 & 데이터 메시지 전송(데이터 파일의 라인 수에 맞춰 메시지 전송. 형식 맞추기 필요!)", notes = "ActiveMQ file & data message send")
    @PostMapping("/activemq/file-data/{taskId}")
    public String activemqFileAndData(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto
    ) {
        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생성

        activeMQRequestLogic.sendFileAndDataTopic(taskId, activeMQRequestFileAndDataDto);
        return "success : taskId : " + taskId;
    }

//    @PostMapping("/activemq")
////    public CompletableFuture<String> activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
//    public String activemq(@RequestBody ActiveMQRequestDto activeMQRequestDto) {
//        // get now time
//        long now = System.currentTimeMillis();
//        String flag = "activemq" + now;
//
//        activeMQRequestLogic.sendTopic(flag, activeMQRequestDto);
//        return "success";
////        return activeMQRequestLogic.sendTopic(flag, activeMQRequestDto)
////                .thenApply(result -> "async success");
//    }

}
