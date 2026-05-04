package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileAndDataDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileDto;
import com.dt.digitaltwinsimulator.logic.ActiveMQRequestLogic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "ActiveMQ Request Controller")
@Slf4j
@RestController
@RequestMapping("/activemq/request")
class ActiveMQRequestController {
    private final ActiveMQRequestLogic activeMQRequestLogic;

    public ActiveMQRequestController(ActiveMQRequestLogic activeMQRequestLogic) {
        this.activeMQRequestLogic = activeMQRequestLogic;
    }

    @Operation(summary = "ActiveMQ 메시지 전송", description = "ActiveMQ message send")
    @PostMapping("/{taskId}")
    public String activemqNormal(
            @PathVariable String taskId,
            @Valid @RequestBody ActiveMQRequestDto activeMQRequestDto
    ) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestDto.getConcurrentTasks());

        for (int i = 0; i < taskCount; i++) {
            String uniqueTaskId = makeUniqueTaskId(taskId, i);
            activeMQRequestLogic.sendTopic(uniqueTaskId, activeMQRequestDto);
        }

        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 메시지 전송(동일한 메시지 반복)", description = "ActiveMQ file message send")
    @PostMapping("/file/{taskId}")
    public String activemqFile(
            @PathVariable String taskId,
            @Valid @RequestBody ActiveMQRequestFileDto activeMQRequestFileDto
    ) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestFileDto.getConcurrentTasks());

        for (int i = 0; i < taskCount; i++) {
            String uniqueTaskId = makeUniqueTaskId(taskId, i);
            activeMQRequestLogic.sendFileTopic(uniqueTaskId, activeMQRequestFileDto);
        }

        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 & 데이터 메시지 전송(데이터 파일의 라인 수에 맞춰 메시지 전송. 형식 맞추기 필요!)", description = "ActiveMQ file & data message send")
    @PostMapping("/file-data/{taskId}")
    public String activemqFileAndData(
            @PathVariable String taskId,
            @Valid @RequestBody ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto
    ) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestFileAndDataDto.getConcurrentTasks());

        for (int i = 0; i < taskCount; i++) {
            String uniqueTaskId = makeUniqueTaskId(taskId, i);
            activeMQRequestLogic.sendFileAndDataTopic(uniqueTaskId, activeMQRequestFileAndDataDto);
        }

        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    private int normalizeConcurrentTasks(int concurrentTasks) {
        if (concurrentTasks < 1) {
            return 1;
        }
        return Math.min(concurrentTasks, 2000);
    }

    private String makeUniqueTaskId(String taskId, int index) {
        return taskId + "-user" + index + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
