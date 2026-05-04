package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileAndDataDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ActiveMQRequestLogic {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{.*?\\}\\}");
    private static final Pattern LEGACY_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter LEGACY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter NORMALIZED_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final TaskCancellationLogic taskCancellationLogic;
    private final ObjectMapper objectMapper;

    public ActiveMQRequestLogic(TaskCancellationLogic taskCancellationLogic) {
        this.taskCancellationLogic = taskCancellationLogic;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<String> sendTopic(String taskId, ActiveMQRequestDto activeMQRequestDto) {
        log.info("taskId : {}", taskId);
        taskCancellationLogic.registerTask(taskId);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQRequestDto.getActiveMQIp());

        try (Connection connection = connectionFactory.createConnection(activeMQRequestDto.getId(), activeMQRequestDto.getPw())) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                 MessageProducer sender = session.createProducer(session.createTopic(activeMQRequestDto.getTopic()))) {

                FormatDefinition formatDefinition = FormatDefinition.from(activeMQRequestDto.getFormat());
                List<String> valueRows = sortedValueRows(activeMQRequestDto.getValue());
                int messageCount = resolveMessageCount(
                        activeMQRequestDto.isRepeatBoolean(),
                        activeMQRequestDto.getRepeatTime(),
                        activeMQRequestDto.getDelayTime(),
                        activeMQRequestDto.getMessageCount()
                );

                if (valueRows.isEmpty()) {
                    sendRandomMessages(taskId, activeMQRequestDto, session, sender, formatDefinition, messageCount);
                } else {
                    sendValueMessages(taskId, activeMQRequestDto, session, sender, formatDefinition, valueRows, messageCount);
                }
            }
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            taskCancellationLogic.removeTask(taskId);
        }

        return CompletableFuture.completedFuture("success");
    }

    private void sendRandomMessages(
            String taskId,
            ActiveMQRequestDto requestDto,
            Session session,
            MessageProducer sender,
            FormatDefinition formatDefinition,
            int messageCount
    ) throws JMSException, InterruptedException, JsonProcessingException {
        for (int i = 0; i < messageCount; i++) {
            if (isCancelled(taskId)) {
                return;
            }

            TextMessage message = session.createTextMessage(createStructuredMessage(requestDto.getTcName(), formatDefinition.randomPayload()));
            log.info("{} random message[{}] : {}", taskId, i, message.getText());
            sender.send(message);
            sleep(requestDto.getDelayTime());
        }
    }

    private void sendValueMessages(
            String taskId,
            ActiveMQRequestDto requestDto,
            Session session,
            MessageProducer sender,
            FormatDefinition formatDefinition,
            List<String> valueRows,
            int repeatCount
    ) throws JMSException, InterruptedException, JsonProcessingException {
        for (int repeatIndex = 0; repeatIndex < repeatCount; repeatIndex++) {
            for (int rowIndex = 0; rowIndex < valueRows.size(); rowIndex++) {
                if (isCancelled(taskId)) {
                    return;
                }

                Map<String, String> payload = formatDefinition.payloadFromValueRow(valueRows.get(rowIndex));
                TextMessage message = session.createTextMessage(createStructuredMessage(requestDto.getTcName(), payload));
                log.info("{} value message[repeat={}, row={}] : {}", taskId, repeatIndex, rowIndex, message.getText());
                sender.send(message);
                sleep(requestDto.getDelayTime());
            }
        }
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<String> sendFileTopic(String taskId, ActiveMQRequestFileDto activeMQRequestFileDto) {
        log.info("taskId : {}", taskId);
        taskCancellationLogic.registerTask(taskId);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQRequestFileDto.getActiveMQIp());

        try (Connection connection = connectionFactory.createConnection(activeMQRequestFileDto.getId(), activeMQRequestFileDto.getPw())) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                 MessageProducer sender = session.createProducer(session.createTopic(activeMQRequestFileDto.getTopic()))) {

                Path filePath = resolveFile(activeMQRequestFileDto.getFilePath(), activeMQRequestFileDto.getFileName());
                String fileContents = Files.readString(filePath, StandardCharsets.UTF_8).trim();
                int messageCount = resolveMessageCount(
                        activeMQRequestFileDto.isRepeatBoolean(),
                        activeMQRequestFileDto.getRepeatTime(),
                        activeMQRequestFileDto.getDelayTime(),
                        activeMQRequestFileDto.getMessageCount()
                );

                for (int i = 0; i < messageCount; i++) {
                    if (isCancelled(taskId)) {
                        return CompletableFuture.completedFuture("Cancelled");
                    }

                    TextMessage message = session.createTextMessage(createFileMessage(activeMQRequestFileDto.getTcName(), taskId, fileContents));
                    log.info("{} file message[{}] : {}", taskId, i, message.getText());
                    sender.send(message);
                    sleep(activeMQRequestFileDto.getDelayTime());
                }
            }
        } catch (JMSException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            taskCancellationLogic.removeTask(taskId);
        }

        return CompletableFuture.completedFuture("success");
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<String> sendFileAndDataTopic(String taskId, ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto) {
        log.info("taskId : {}", taskId);
        taskCancellationLogic.registerTask(taskId);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQRequestFileAndDataDto.getActiveMQIp());

        try (Connection connection = connectionFactory.createConnection(activeMQRequestFileAndDataDto.getId(), activeMQRequestFileAndDataDto.getPw())) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                 MessageProducer sender = session.createProducer(session.createTopic(activeMQRequestFileAndDataDto.getTopic()))) {

                Path formatFilePath = resolveFile(activeMQRequestFileAndDataDto.getFilePath(), activeMQRequestFileAndDataDto.getFormatFileName());
                Path dataFilePath = resolveFile(activeMQRequestFileAndDataDto.getFilePath(), activeMQRequestFileAndDataDto.getDataFileName());
                String originFormatContent = Files.readString(formatFilePath, StandardCharsets.UTF_8);
                List<String[]> dataLines = readDataLines(dataFilePath);

                for (int rowIndex = 0; rowIndex < dataLines.size(); rowIndex++) {
                    if (isCancelled(taskId)) {
                        return CompletableFuture.completedFuture("Cancelled");
                    }

                    String renderedFormat = renderTemplate(originFormatContent, dataLines.get(rowIndex));
                    TextMessage message = session.createTextMessage(createFileDataMessage(activeMQRequestFileAndDataDto.getTcName(), renderedFormat));
                    log.info("{} file-data message[row={}] : {}", taskId, rowIndex, message.getText());
                    sender.send(message);
                    sleep(activeMQRequestFileAndDataDto.getDelayTime());
                }
            }
        } catch (JMSException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            taskCancellationLogic.removeTask(taskId);
        }

        return CompletableFuture.completedFuture("success");
    }

    private boolean isCancelled(String taskId) {
        if (taskCancellationLogic.isCancellationRequested(taskId)) {
            log.info("작업이 취소되었습니다: {}", taskId);
            return true;
        }
        return false;
    }

    private String createStructuredMessage(String tcName, Map<String, String> payload) throws JsonProcessingException {
        String nowString = nowTimestamp();

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put(nowString, payload);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("CREATE_TIMESTAMP", nowString);
        root.put("MESSAGE_ID", tcName);
        root.put("DATA_MAP", dataMap);

        return objectMapper.writeValueAsString(root);
    }

    private String createFileMessage(String tcName, String taskId, String fileContents) {
        String nowString = nowTimestamp();
        String normalizedFileContents = removeOuterBraces(fileContents);

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"CREATE_TIMESTAMP\":\"").append(escapeJsonString(nowString)).append("\",");
        sb.append("\"MESSAGE_ID\":\"").append(escapeJsonString(tcName)).append("\",");
        sb.append("\"DATA_MAP\":{");
        sb.append('\"').append(escapeJsonString(nowString)).append("\":{");
        sb.append("\"USER_ID\":\"").append(escapeJsonString(taskId)).append('\"');
        if (!normalizedFileContents.isBlank()) {
            sb.append(',').append(normalizedFileContents);
        }
        sb.append("}}}");
        return sb.toString();
    }

    private String createFileDataMessage(String tcName, String formatContent) {
        String nowString = nowTimestamp();
        String normalizedFormatContent = removeOuterBraces(formatContent.trim());

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"CREATE_TIMESTAMP\":\"").append(escapeJsonString(nowString)).append("\",");
        sb.append("\"MESSAGE_ID\":\"").append(escapeJsonString(tcName)).append("\",");
        sb.append("\"DATA_MAP\":{");
        sb.append('\"').append(escapeJsonString(nowString)).append("\":{");
        sb.append("\"transaction_code\":\"").append(escapeJsonString(tcName)).append("\",");
        sb.append("\"works_code\":\"K\",");
        sb.append("\"sndr_inform_edit_pgm_id\":\"\",");
        sb.append("\"eai_interface_id\":\"\",");
        sb.append("\"interface_data_dir_actual_type\":\"\",");
        sb.append("\"interface_data_ocr_res_flag\":\"\",");
        sb.append("\"interface_data_send_seq\":\"0\",");
        sb.append("\"interface_data_upd_tp\":\"\",");
        sb.append("\"interface_data_t_len\":\"760\",");
        sb.append("\"attribute\":\" \",");
        sb.append("\"bsc_gw_data_attr\":\" \",");
        sb.append("\"it_com_eai_ifc_var_item_usg_f\":\" \"");
        if (!normalizedFormatContent.isBlank()) {
            sb.append(',').append(normalizedFormatContent);
        }
        sb.append("}}}");
        return sb.toString();
    }

    private String renderTemplate(String template, String[] data) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        int dataIndex = 0;

        while (matcher.find()) {
            if (dataIndex >= data.length) {
                throw new IllegalArgumentException("데이터 컬럼 수가 포맷 파일의 placeholder 수보다 적습니다.");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(data[dataIndex++]));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private List<String[]> readDataLines(Path dataFilePath) throws IOException {
        try (Stream<String> lines = Files.lines(dataFilePath, StandardCharsets.UTF_8)) {
            return lines
                    .filter(line -> !line.isBlank())
                    .map(this::splitCsvLine)
                    .collect(Collectors.toList());
        }
    }

    private String[] splitCsvLine(String line) {
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = normalizeDataValue(parts[i].trim());
        }
        return parts;
    }

    private String normalizeDataValue(String value) {
        if (LEGACY_DATE_PATTERN.matcher(value).matches()) {
            return LocalDateTime.parse(value, LEGACY_DATE_FORMATTER).format(NORMALIZED_DATE_FORMATTER);
        }
        return value;
    }

    private List<String> sortedValueRows(List<Map<Integer, String>> value) {
        if (value == null || value.isEmpty() || value.get(0) == null || value.get(0).isEmpty()) {
            return List.of();
        }

        return value.get(0).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(row -> !row.isBlank())
                .collect(Collectors.toList());
    }

    private int resolveMessageCount(boolean repeatBoolean, int repeatTime, int delayTime, int explicitMessageCount) {
        if (explicitMessageCount > 0) {
            return explicitMessageCount;
        }
        if (!repeatBoolean) {
            return 1;
        }
        if (repeatTime <= 0) {
            return 1;
        }

        int interval = delayTime > 0 ? delayTime : 1000;
        return Math.max(1, (int) Math.ceil((double) repeatTime / interval));
    }

    private Path resolveFile(String directory, String filename) {
        Path basePath = Paths.get(directory).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(filename).normalize();

        if (!targetPath.startsWith(basePath)) {
            throw new IllegalArgumentException("허용되지 않은 파일 경로입니다: " + filename);
        }
        if (!Files.isRegularFile(targetPath)) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + targetPath);
        }

        return targetPath;
    }

    private String nowTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    private void sleep(int delayTime) throws InterruptedException {
        if (delayTime > 0) {
            Thread.sleep(delayTime);
        }
    }

    private String removeOuterBraces(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private String escapeJsonString(String value) {
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

    public String prettyPrintUsingGlobalSetting(String uglyJsonString) throws JsonProcessingException {
        Object jsonObject = objectMapper.readValue(uglyJsonString, Object.class);
        return objectMapper.writeValueAsString(jsonObject);
    }

    private record FormatDefinition(
            List<String> dataIds,
            List<String> dataTypes,
            List<String> randomBooleans,
            List<String> randomConditions
    ) {
        private static FormatDefinition from(List<Map<String, String>> format) {
            if (format == null || format.isEmpty() || format.get(0) == null) {
                throw new IllegalArgumentException("format 정보가 필요합니다.");
            }

            Map<String, String> firstFormat = format.get(0);
            List<String> dataIds = splitRequired(firstFormat.get("dataId"), "dataId");
            List<String> dataTypes = splitWithDefault(firstFormat.get("dataType"), dataIds.size(), "String");
            List<String> randomBooleans = splitWithDefault(firstFormat.get("randomBoolean"), dataIds.size(), "0");
            List<String> randomConditions = splitWithDefault(firstFormat.get("randomCondition"), dataIds.size(), "");

            return new FormatDefinition(dataIds, dataTypes, randomBooleans, randomConditions);
        }

        private Map<String, String> randomPayload() {
            Map<String, String> payload = new LinkedHashMap<>();
            for (int i = 0; i < dataIds.size(); i++) {
                payload.put(dataIds.get(i), RandomValueGenerator.generate(dataTypes.get(i), randomConditions.get(i)));
            }
            return payload;
        }

        private Map<String, String> payloadFromValueRow(String row) {
            String[] values = row.split(",", -1);
            Map<String, String> payload = new LinkedHashMap<>();

            for (int i = 0; i < dataIds.size(); i++) {
                if (RandomValueGenerator.isRandomEnabled(randomBooleans.get(i))) {
                    payload.put(dataIds.get(i), RandomValueGenerator.generate(dataTypes.get(i), randomConditions.get(i)));
                    continue;
                }

                if (i >= values.length) {
                    throw new IllegalArgumentException("value 컬럼 수가 format dataId 수보다 적습니다. row=" + row);
                }
                payload.put(dataIds.get(i), values[i].trim());
            }
            return payload;
        }

        private static List<String> splitRequired(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("format." + fieldName + " 값이 필요합니다.");
            }
            return split(value);
        }

        private static List<String> splitWithDefault(String value, int size, String defaultValue) {
            List<String> values = value == null || value.isBlank() ? new ArrayList<>() : split(value);
            while (values.size() < size) {
                values.add(defaultValue);
            }
            if (values.size() > size) {
                return values.subList(0, size);
            }
            return values;
        }

        private static List<String> split(String value) {
            return Stream.of(value.split(",", -1))
                    .map(String::trim)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
