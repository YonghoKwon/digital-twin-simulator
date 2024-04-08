package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.dto.ActiveMQTaskInfoDto;
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
    @Value("${server.port}")
    private String serverPort;

    private final TaskCancellationLogic taskCancellationLogic;

    public ActiveMQTaskLogic(TaskCancellationLogic taskCancellationLogic) {
        this.taskCancellationLogic = taskCancellationLogic;
    }

    public List<ActiveMQTaskInfoDto> makeTaskInfoList() {
        List<ActiveMQTaskInfoDto> taskInfoList = new ArrayList<>();

        Set<String> runningTaskIds = taskCancellationLogic.getRunningTaskIds();

        // runningTaskIds sorted by ascending order
        List<String> sortedTaskIdList = new ArrayList<>(runningTaskIds);
        Collections.sort(sortedTaskIdList); // 오름차순 정렬
//        Collections.sort(sortedTaskIdList, Collections.reverseOrder()); // 내림차순 정렬

        String baseUrl = "http://localhost:" + serverPort;
        sortedTaskIdList.forEach(taskId ->
                taskInfoList.add(new ActiveMQTaskInfoDto(taskId, baseUrl + "/cancel-task/" + taskId))
        );

        return taskInfoList;
    }
}
