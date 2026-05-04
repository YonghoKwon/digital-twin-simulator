package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.config.ActiveMqBrokerProperties;
import com.dt.digitaltwinsimulator.entity.dto.JmsTemplateLoadTestRequestDto;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JmsTemplateLoadTestLogic {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ActiveMqBrokerProperties brokerProperties;
    private final TaskCancellationLogic taskCancellationLogic;
    private final TaskExecutionStatusLogic taskExecutionStatusLogic;

    public JmsTemplateLoadTestLogic(
            ActiveMqBrokerProperties brokerProperties,
            TaskCancellationLogic taskCancellationLogic,
            TaskExecutionStatusLogic taskExecutionStatusLogic
    ) {
        this.brokerProperties = brokerProperties;
        this.taskCancellationLogic = taskCancellationLogic;
        this.taskExecutionStatusLogic = taskExecutionStatusLogic;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<String> run(String taskId, JmsTemplateLoadTestRequestDto requestDto) {
        taskCancellationLogic.registerTask(taskId);
        taskExecutionStatusLogic.markRunning(taskId);

        CachingConnectionFactory cachingConnectionFactory = null;
        ExecutorService executorService = null;

        try {
            ConnectionFactory targetFactory = new ActiveMQConnectionFactory(
                    brokerProperties.getBrokerUrl(),
                    brokerProperties.getUsername(),
                    brokerProperties.getPassword()
            );
            cachingConnectionFactory = new CachingConnectionFactory(targetFactory);
            cachingConnectionFactory.setSessionCacheSize(Math.max(1, brokerProperties.getSessionCacheSize()));

            JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
            jmsTemplate.setPubSubDomain(true);

            String topic = isBlank(requestDto.getTopic()) ? brokerProperties.getTopic() : requestDto.getTopic();
            int workerCount = Math.max(1, requestDto.getWorkerCount());
            int totalMessageCount = Math.max(1, requestDto.getMessageCount());
            AtomicInteger nextIndex = new AtomicInteger(0);
            executorService = Executors.newFixedThreadPool(workerCount);
            List<Future<?>> futures = new ArrayList<>();

            for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
                int currentWorkerIndex = workerIndex;
                futures.add(executorService.submit(() -> {
                    while (true) {
                        if (taskCancellationLogic.isCancellationRequested(taskId)) {
                            return;
                        }

                        int messageIndex = nextIndex.getAndIncrement();
                        if (messageIndex >= totalMessageCount) {
                            return;
                        }

                        String message = createMessage(requestDto.getTcName(), currentWorkerIndex, messageIndex, requestDto.getPayload());
                        jmsTemplate.convertAndSend(topic, message);
                        taskExecutionStatusLogic.incrementSentCount(taskId);
                        sleep(requestDto.getDelayTime());
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            if (taskCancellationLogic.isCancellationRequested(taskId)) {
                taskExecutionStatusLogic.markCancelled(taskId);
                return CompletableFuture.completedFuture("cancelled");
            }

            taskExecutionStatusLogic.markSuccess(taskId);
            return CompletableFuture.completedFuture("success");
        } catch (Exception e) {
            taskExecutionStatusLogic.markFailed(taskId, e);
            throw new RuntimeException(e);
        } finally {
            taskCancellationLogic.removeTask(taskId);
            if (executorService != null) {
                executorService.shutdownNow();
            }
            if (cachingConnectionFactory != null) {
                cachingConnectionFactory.destroy();
            }
        }
    }

    private String createMessage(String tcName, int workerIndex, int messageIndex, String payload) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String normalizedPayload = normalizePayload(payload);

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"CREATE_TIMESTAMP\":\"").append(timestamp).append("\",");
        sb.append("\"MESSAGE_ID\":\"").append(escape(tcName)).append("\",");
        sb.append("\"DATA_MAP\":{");
        sb.append('\"').append(timestamp).append("\":{");
        sb.append("\"workerIndex\":").append(workerIndex).append(',');
        sb.append("\"messageIndex\":").append(messageIndex);
        if (!normalizedPayload.isBlank()) {
            sb.append(',').append(normalizedPayload);
        }
        sb.append("}}}");
        return sb.toString();
    }

    private String normalizePayload(String payload) {
        String value = payload == null ? "{}" : payload.trim();
        if (value.startsWith("{") && value.endsWith("}") && value.length() >= 2) {
            return value.substring(1, value.length() - 1).trim();
        }
        return "\"payload\":\"" + escape(value) + "\"";
    }

    private void sleep(int delayTime) {
        if (delayTime <= 0) {
            return;
        }
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
