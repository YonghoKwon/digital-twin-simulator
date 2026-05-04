package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQTaskInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ActiveMQTaskLogic {
    @Value("${server.port:8080}")
    private String serverPort;

    private final TaskCancellationLogic taskCancellationLogic;

    public ActiveMQTaskLogic(TaskCancellationLogic taskCancellationLogic) {
        this.taskCancellationLogic = taskCancellationLogic;
    }

    public List<ActiveMQTaskInfoDto> makeTaskInfoList() {
        List<ActiveMQTaskInfoDto> taskInfoList = new ArrayList<>();
        Set<String> runningTaskIds = taskCancellationLogic.getRunningTaskIds();

        List<String> sortedTaskIdList = new ArrayList<>(runningTaskIds);
        Collections.sort(sortedTaskIdList);

        String baseUrl = "http://localhost:" + serverPort;
        sortedTaskIdList.forEach(taskId ->
                taskInfoList.add(new ActiveMQTaskInfoDto(taskId, baseUrl + "/activemq/task/cancel-task/" + taskId))
        );

        return taskInfoList;
    }
}
