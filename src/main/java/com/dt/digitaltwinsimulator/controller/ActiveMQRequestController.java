package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileAndDataDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileDto;
import com.dt.digitaltwinsimulator.logic.ActiveMQRequestLogic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "ActiveMQ Request Controller")
@Slf4j
@RestController
@RequestMapping("/activemq/request")
class ActiveMQRequestController {

    private final ActiveMQRequestLogic activeMQRequestLogic;

    public ActiveMQRequestController(
            ActiveMQRequestLogic activeMQRequestLogic
    ) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @Operation(summary = "ActiveMQ 메시지 전송", description = "ActiveMQ message send")
    @PostMapping("/{taskId}")
    public String activemqNormal(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestDto activeMQRequestDto
    ) {
//        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생
//
//        activeMQRequestLogic.sendTopic(taskId, activeMQRequestDto);
//        return "success : taskId : " + taskId;

        int userCount = activeMQRequestDto.getConcurrentTasks();
        if (userCount < 1) userCount = 1;

        for (int i = 0; i < userCount; i++) {
            String uniqueTaskId = taskId + "-user" + i + "-" + UUID.randomUUID().toString().substring(0, 8);

            // 가상 스레드에서 병렬 실행
            activeMQRequestLogic.sendTopic(uniqueTaskId, activeMQRequestDto);
        }

        return "success : Started " + userCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 메시지 전송(동일한 메시지 반복)", description =  "ActiveMQ file message send")
    @PostMapping("/file/{taskId}")
    public String activemqFile(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestFileDto activeMQRequestFileDto
    ) {
//        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생성
//
//        activeMQRequestLogic.sendFileTopic(taskId, activeMQRequestFileDto);
//        return "success : taskId : " + taskId;

        int userCount = activeMQRequestFileDto.getConcurrentTasks();
        if (userCount < 1) userCount = 1;

        for (int i = 0; i < userCount; i++) {
            String uniqueTaskId = taskId + "-user" + i + "-" + UUID.randomUUID().toString().substring(0, 8);

            // 가상 스레드에서 병렬 실행
            activeMQRequestLogic.sendFileTopic(uniqueTaskId, activeMQRequestFileDto);
        }

        return "success : Started " + userCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 & 데이터 메시지 전송(데이터 파일의 라인 수에 맞춰 메시지 전송. 형식 맞추기 필요!)", description =  "ActiveMQ file & data message send")
    @PostMapping("/file-data/{taskId}")
    public String activemqFileAndData(
            @PathVariable String taskId,
            @RequestBody ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto
    ) {
//        taskId = taskId + "-" + UUID.randomUUID(); // 고유한 작업 ID 생성
//
//        activeMQRequestLogic.sendFileAndDataTopic(taskId, activeMQRequestFileAndDataDto);
//        return "success : taskId : " + taskId;

        int userCount = activeMQRequestFileAndDataDto.getConcurrentTasks();
        if (userCount < 1) userCount = 1;

        for (int i = 0; i < userCount; i++) {
            String uniqueTaskId = taskId + "-user" + i + "-" + UUID.randomUUID().toString().substring(0, 8);

            // 가상 스레드에서 병렬 실행
            activeMQRequestLogic.sendFileAndDataTopic(uniqueTaskId, activeMQRequestFileAndDataDto);
        }

        return "success : Started " + userCount + " tasks with base ID " + taskId;
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
