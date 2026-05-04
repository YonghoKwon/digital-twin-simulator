package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQTaskInfoDto;
import com.dt.digitaltwinsimulator.logic.ActiveMQTaskLogic;
import com.dt.digitaltwinsimulator.logic.TaskCancellationLogic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ActiveMQ Task Controller")
@RestController
@RequestMapping("/activemq/task")
public class ActiveMQTaskController {
    private final ActiveMQTaskLogic activeMQTaskLogic;
    private final TaskCancellationLogic taskCancellationLogic;

    public ActiveMQTaskController(
            ActiveMQTaskLogic activeMQTaskLogic,
            TaskCancellationLogic taskCancellationLogic
    ) {
        this.activeMQTaskLogic = activeMQTaskLogic;
        this.taskCancellationLogic = taskCancellationLogic;
    }

    @Operation(summary = "작동 중인 모든 task 조회", description = "Get running tasks")
    @GetMapping("/running-tasks")
    public ResponseEntity<List<ActiveMQTaskInfoDto>> getRunningTasks() {
        return ResponseEntity.ok(activeMQTaskLogic.makeTaskInfoList());
    }

    @Operation(summary = "모든 taskId에 해당 하는 task 취소", description = "cancel all tasks")
    @PostMapping("/cancel-tasks")
    public ResponseEntity<String> cancelAllTasks() {
        int requestedCount = taskCancellationLogic.requestAllCancellation();
        return ResponseEntity.ok("작업 취소 요청됨: " + requestedCount + "건");
    }

    @Operation(summary = "특정 taskId에 해당 하는 task 취소", description = "cancel task by taskId")
    @PostMapping("/cancel-task/{taskId}")
    public ResponseEntity<String> cancelTask(@PathVariable String taskId) {
        boolean requested = taskCancellationLogic.requestCancellation(taskId);
        if (!requested) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("작업 취소 요청됨: " + taskId);
    }
}
