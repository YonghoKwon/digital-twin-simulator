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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
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
    public String activemqNormal(@PathVariable String taskId, @Valid @RequestBody ActiveMQRequestDto activeMQRequestDto) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestDto.getConcurrentTasks());
        for (int i = 0; i < taskCount; i++) {
            activeMQRequestLogic.sendTopic(makeUniqueTaskId(taskId, i), activeMQRequestDto);
        }
        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 메시지 Dry-run", description = "Generate sample messages without sending to ActiveMQ")
    @PostMapping("/dry-run")
    public List<String> activemqNormalDryRun(
            @Valid @RequestBody ActiveMQRequestDto activeMQRequestDto,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception {
        return activeMQRequestLogic.dryRunTopic(activeMQRequestDto, limit);
    }

    @Operation(summary = "ActiveMQ 파일 메시지 전송(동일한 메시지 반복)", description = "ActiveMQ file message send")
    @PostMapping("/file/{taskId}")
    public String activemqFile(@PathVariable String taskId, @Valid @RequestBody ActiveMQRequestFileDto activeMQRequestFileDto) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestFileDto.getConcurrentTasks());
        for (int i = 0; i < taskCount; i++) {
            activeMQRequestLogic.sendFileTopic(makeUniqueTaskId(taskId, i), activeMQRequestFileDto);
        }
        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 메시지 Dry-run", description = "Generate file messages without sending to ActiveMQ")
    @PostMapping("/file/dry-run")
    public List<String> activemqFileDryRun(
            @Valid @RequestBody ActiveMQRequestFileDto activeMQRequestFileDto,
            @RequestParam(defaultValue = "10") int limit
    ) throws IOException {
        return activeMQRequestLogic.dryRunFileTopic(activeMQRequestFileDto, limit);
    }

    @Operation(summary = "ActiveMQ 파일 & 데이터 메시지 전송", description = "ActiveMQ file & data message send")
    @PostMapping("/file-data/{taskId}")
    public String activemqFileAndData(@PathVariable String taskId, @Valid @RequestBody ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto) {
        int taskCount = normalizeConcurrentTasks(activeMQRequestFileAndDataDto.getConcurrentTasks());
        for (int i = 0; i < taskCount; i++) {
            activeMQRequestLogic.sendFileAndDataTopic(makeUniqueTaskId(taskId, i), activeMQRequestFileAndDataDto);
        }
        return "success : Started " + taskCount + " tasks with base ID " + taskId;
    }

    @Operation(summary = "ActiveMQ 파일 & 데이터 메시지 Dry-run", description = "Generate file-data messages without sending to ActiveMQ")
    @PostMapping("/file-data/dry-run")
    public List<String> activemqFileAndDataDryRun(
            @Valid @RequestBody ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto,
            @RequestParam(defaultValue = "10") int limit
    ) throws IOException {
        return activeMQRequestLogic.dryRunFileAndDataTopic(activeMQRequestFileAndDataDto, limit);
    }

    private int normalizeConcurrentTasks(int concurrentTasks) {
        if (concurrentTasks < 1) return 1;
        return Math.min(concurrentTasks, 2000);
    }

    private String makeUniqueTaskId(String taskId, int index) {
        return taskId + "-user" + index + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
