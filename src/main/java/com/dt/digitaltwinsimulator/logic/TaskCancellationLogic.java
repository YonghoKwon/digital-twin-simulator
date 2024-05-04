package com.dt.digitaltwinsimulator.logic;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TaskCancellationLogic {
    private final ConcurrentHashMap<String, AtomicBoolean> cancellationTokens = new ConcurrentHashMap<>();

    public Set<String> getRunningTaskIds() {
        return cancellationTokens.keySet();
    }

    // 작업 등록
    public void registerTask(String taskId) {
        cancellationTokens.putIfAbsent(taskId, new AtomicBoolean(false));
    }

    public void requestCancellation(String taskId) {
        cancellationTokens.computeIfAbsent(taskId, k -> new AtomicBoolean()).set(true);
    }

    public boolean isCancellationRequested(String taskId) {
        return cancellationTokens.getOrDefault(taskId, new AtomicBoolean(false)).get();
    }

    public void removeTask(String taskId) {
        cancellationTokens.remove(taskId);
    }

    public void requestAllCancellation() {
        // all tasks cancel
        cancellationTokens.forEach((k, v) -> v.set(true));
    }
}

