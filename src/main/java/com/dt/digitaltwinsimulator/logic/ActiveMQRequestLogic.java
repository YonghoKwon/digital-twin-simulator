package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileAndDataDto;
import com.dt.digitaltwinsimulator.entity.dto.ActiveMQRequestFileDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActiveMQRequestLogic {

    private final TaskCancellationLogic taskCancellationLogic;

    public ActiveMQRequestLogic(TaskCancellationLogic taskCancellationLogic) {
        this.taskCancellationLogic = taskCancellationLogic;
    }

    @Async
    public CompletableFuture<String> sendTopic(String taskId, ActiveMQRequestDto activeMQRequestDto) {
        log.info("taskId : " + taskId);
        taskCancellationLogic.registerTask(taskId);

        return CompletableFuture.supplyAsync(() -> {
            // activeMQ connection
            ConnectionFactory connectionFactory = new JmsConnectionFactory(activeMQRequestDto.getActiveMQIp());

            try(Connection connection = connectionFactory.createConnection(activeMQRequestDto.getId(), activeMQRequestDto.getPw())) {
                connection.start();

                // session, topic, producer create
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(activeMQRequestDto.getTopic());
                MessageProducer sender = session.createProducer(topic);

                // 분기 처리.. 랜덤 항목이 true면 반복시간만큼 message 만들어서 send
                if(activeMQRequestDto.isRepeatBoolean()) {
                    int i = 0;
                    while (i < (activeMQRequestDto.getRepeatTime() / 1000) ) {
                        i++;

                        // 작업 취소 확인 로직
                        String cancelled = taskCancellation(taskId, sender, session);
                        if (cancelled != null) return cancelled;

                        // value count가 1개 이상일 때
                        if(!activeMQRequestDto.getValue().get(0).isEmpty()) {
                            // message creates in value count
                            messageCreateInValueCount(taskId, activeMQRequestDto, session, sender);
                        } else {
                            // message creates in random
                            messageCreateRandom(taskId, activeMQRequestDto, session, sender);
                        }

                    }
                    // connection close;
                    sender.close();
                    session.close();
                } else {
                    // 작업 취소 확인 로직
                    String cancelled = taskCancellation(taskId, sender, session);
                    if (cancelled != null) return cancelled;

                    // message creates in value count
                    messageCreateInValueCount(taskId, activeMQRequestDto, session, sender);

                    // connection close;
                    sender.close();
                    session.close();
                }

            } catch (JMSException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                taskCancellationLogic.removeTask(taskId);
            }

            return "success";
        });
    }

    private String taskCancellation(String taskId, MessageProducer sender, Session session) throws JMSException {
        // 작업 취소 확인 로직
        if (taskCancellationLogic.isCancellationRequested(taskId)) {
            log.info("작업이 취소되었습니다: " + taskId);

            // connection close;
            sender.close();
            session.close();

            // 필요한 경우 여기에서 작업 종료 관련 정리를 수행할 수 있습니다.
            return "Cancelled";
        }
        return null;
    }

    private void messageCreateRandom(String flag, ActiveMQRequestDto activeMQRequestDto, Session session, MessageProducer sender) throws JMSException, InterruptedException {
        TextMessage message = session.createTextMessage();

        Date now = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
        String nowString = simpleDateFormat.format(now);

        String[] keyArray = activeMQRequestDto.getFormat().get(0).get("dataId").split(",");
        String[] keyTypeArray = activeMQRequestDto.getFormat().get(0).get("dataType").split(",");
        String[] randomBooleanArray = activeMQRequestDto.getFormat().get(0).get("randomBoolean").split(",");
        String[] randomConditionArray = activeMQRequestDto.getFormat().get(0).get("randomCondition").split(",");

        message.setText(
            "{" +
                "\"CREATE_TIMESTAMP\": \"" + nowString + "\"," +
                "\"MESSAGE_ID\": \"" + activeMQRequestDto.getTcName() + "\"," +
                "\"DATA_MAP\": {" +
                    "\"" + nowString + "\":{" +
                    // Key: Value
                    // make key and value in keyArray size
                    makeRandomData(keyArray, keyTypeArray, randomBooleanArray, randomConditionArray) +
                    "}" +
                "}" +
            "}"
        );

        log.info(flag + " message : " + message.getText());
//                        log.info(flag + " message : " + prettyPrintUsingGlobalSetting(message.getText()));

        // message send
        sender.send(message);

        Thread.sleep(activeMQRequestDto.getDelayTime());
    }

    private void messageCreateInValueCount(String flag, ActiveMQRequestDto activeMQRequestDto, Session session, MessageProducer sender) throws JMSException, InterruptedException {
        for (int i = 0; i < (activeMQRequestDto.getValue().get(0)).size(); i++) {
            TextMessage message = session.createTextMessage();

            Date now = new Date();
            Locale currentLocale = new Locale("KOREAN", "KOREA");
            String pattern = "yyyyMMddHHmmss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
            String nowString = simpleDateFormat.format(now);

            String[] keyArray = activeMQRequestDto.getFormat().get(0).get("dataId").split(",");
            String[] valueArray = activeMQRequestDto.getValue().get(0).get(i).split(",");

            String[] keyTypeArray = activeMQRequestDto.getFormat().get(0).get("dataType").split(",");
            String[] randomBooleanArray = activeMQRequestDto.getFormat().get(0).get("randomBoolean").split(",");
            String[] randomConditionArray = activeMQRequestDto.getFormat().get(0).get("randomCondition").split(",");

            message.setText(
                "{" +
                    "\"CREATE_TIMESTAMP\": \"" + nowString + "\"," +
                    "\"MESSAGE_ID\": \"" + activeMQRequestDto.getTcName() + "\"," +
                    "\"DATA_MAP\": {" +
                        "\"" + nowString + "\":{" +
                        // Key: Value
                        // make key and value in keyArray size
//                                    "\"" + keyArray[j] + "\": \"" + valueArray[j] + "\"," +
                        makeData(keyArray, keyTypeArray, randomBooleanArray, randomConditionArray, valueArray) +
                        "}" +
                    "}" +
                "}"
            );

            log.info(flag + " message : " + message.getText());
//                        log.info(flag + " message : " + prettyPrintUsingGlobalSetting(message.getText()));

            // message send
            sender.send(message);

            Thread.sleep(activeMQRequestDto.getDelayTime());
        }
    }

    private String makeRandomData(String[] keyArray, String[] keyTypeArray, String[] randomBooleanArray, String[] randomConditionArray) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < keyArray.length; i++) {
            if(i == keyArray.length - 1)
                sb.append("\"" + keyArray[i].trim() + "\": \"" + makeRandomValue(keyTypeArray[i].trim(), randomConditionArray[i].trim()) + "\"");
            else
                sb.append("\"" + keyArray[i].trim() + "\": \"" + makeRandomValue(keyTypeArray[i].trim(), randomConditionArray[i].trim()) + "\",");
        }
        return sb.toString();
    }

    private String makeRandomValue(String keyType, String randomCondition) {
        String result = "";
        switch (keyType) {
            case "String":
                result = randomString(Integer.parseInt(randomCondition));
                break;
            case "Integer":
                result = randomInteger(Integer.parseInt(randomCondition));
                break;
            case "Double":
                result = randomDouble(Double.parseDouble(randomCondition));
                break;
            case "Boolean":
                result = randomBoolean();
                break;
            case "Date":
                result = randomDate();
                break;
            default:
                break;
        }
        return result;

    }

    private String randomString(int i) {
        // i ~ j 길이의 랜덤 문자열 생성하여 리턴
        return RandomStringUtils.randomAlphanumeric(i);
    }

    private String randomInteger(int i) {
        // i ~ j 범위의 랜덤 정수 생성하여 리턴
        return String.valueOf(RandomUtil.randomInterval(0, i));
    }

    private String randomDouble(double i) {
        // i ~ j 범위 값 랜덤 생성
        // k 자리수로 반올림
        double randomDouble = i + new Random().nextDouble() * (0 - i);

        // randomDouble을 3자리수로 반올림
        randomDouble = Math.round(randomDouble * 1000) / 1000.0;

        return String.valueOf(randomDouble);
    }

    private String randomBoolean() {
        return null;
    }

    private String randomDate() {
        return null;
    }

    private String makeData(String[] keyArray, String[] keyTypeArray, String[] randomBooleanArray, String[] randomConditionArray, String[] valueArray) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < keyArray.length; i++) {
            if(i == keyArray.length - 1)
                if(randomBooleanArray[i].trim().equals("1"))
                    sb.append("\"" + keyArray[i].trim() + "\": \"" + makeRandomValue(keyTypeArray[i].trim(), randomConditionArray[i].trim()) + "\"");
                else
                    sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\"");
//                sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\"");
            else
                if (randomBooleanArray[i].trim().equals("1"))
                    sb.append("\"" + keyArray[i].trim() + "\": \"" + makeRandomValue(keyTypeArray[i].trim(), randomConditionArray[i].trim()) + "\",");
                else
                    sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\",");
//                sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\",");
        }
        return sb.toString();
    }

    public String prettyPrintUsingGlobalSetting(String uglyJsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object jsonObject = mapper.readValue(uglyJsonString, Object.class);
        return mapper.writeValueAsString(jsonObject);
    }

    public CompletableFuture<String> sendFileTopic(String taskId, ActiveMQRequestFileDto activeMQRequestFileDto) {
        log.info("taskId : " + taskId);
        taskCancellationLogic.registerTask(taskId);

        return CompletableFuture.supplyAsync(() -> {
            // activeMQ connection
            ConnectionFactory connectionFactory = new JmsConnectionFactory(activeMQRequestFileDto.getActiveMQIp());

            try(Connection connection = connectionFactory.createConnection(activeMQRequestFileDto.getId(), activeMQRequestFileDto.getPw())) {
                connection.start();

                // session, topic, producer create
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(activeMQRequestFileDto.getTopic());
                MessageProducer sender = session.createProducer(topic);

                // local file read
                String DATA_DIRECTORY = activeMQRequestFileDto.getFilePath();
                File dir = new File(DATA_DIRECTORY);

                String[] filenames = dir.list();
                // When filename is in filenames, send message for contents in file
                for (String filename : Objects.requireNonNull(filenames)) {
                    if(Objects.equals(filename, activeMQRequestFileDto.getFileName())) {

                        // 반복처리 플래그가 True면 반복시간만큼 message 만들어서 send
                        if(activeMQRequestFileDto.isRepeatBoolean()) {
                            int i = 0;
                            while (i < (activeMQRequestFileDto.getRepeatTime() / 1000) ) {
                                i++;

                                // 작업 취소 확인 로직
                                String cancelled = taskCancellation(taskId, sender, session);
                                if (cancelled != null) return cancelled;

                                TextMessage message = session.createTextMessage();

                                Date now = new Date();
                                Locale currentLocale = new Locale("KOREAN", "KOREA");
                                String pattern = "yyyyMMddHHmmss";
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
                                String nowString = simpleDateFormat.format(now);

                                // file read
                                String fileContents = "";
                                try {
                                    fileContents = new String(java.nio.file.Files.readAllBytes(new File(DATA_DIRECTORY + filename).toPath()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                message.setText(
                                        "{" +
                                            "\"CREATE_TIMESTAMP\": \"" + nowString + "\"," +
                                            "\"MESSAGE_ID\": \"" + activeMQRequestFileDto.getTcName() + "\"," +
                                            "\"DATA_MAP\": {" +
                                                "\"" + nowString + "\":{" +
                                                    fileContents +
                                                "}" +
                                            "}" +
                                        "}"
                                );

//                                log.info("message : " + prettyPrintUsingGlobalSetting(message.getText()));

                                // message send
                                sender.send(message);
                                Thread.sleep(activeMQRequestFileDto.getDelayTime());
                            }
                        }
                    }
                }

                // connection close;
                sender.close();
                session.close();

            } catch (JMSException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                taskCancellationLogic.removeTask(taskId);
            }

            return "success";
        });
    }

    public CompletableFuture<String> sendFileAndDataTopic(String taskId, ActiveMQRequestFileAndDataDto activeMQRequestFileAndDataDto) {
        log.info("taskId : " + taskId);
        taskCancellationLogic.registerTask(taskId);

        return CompletableFuture.supplyAsync(() -> {
            // activeMQ connection
            ConnectionFactory connectionFactory = new JmsConnectionFactory(activeMQRequestFileAndDataDto.getActiveMQIp());

            try(Connection connection = connectionFactory.createConnection(activeMQRequestFileAndDataDto.getId(), activeMQRequestFileAndDataDto.getPw())) {
                connection.start();

                // session, topic, producer create
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(activeMQRequestFileAndDataDto.getTopic());
                MessageProducer sender = session.createProducer(topic);

                // local file read
                String DATA_DIRECTORY = activeMQRequestFileAndDataDto.getFilePath();
                String FORMAT_FILENAME = activeMQRequestFileAndDataDto.getFormatFileName();
                String DATA_FILENAME = activeMQRequestFileAndDataDto.getDataFileName();

                // 포멧 파일에서 내용 읽기
                String formatContent = new String(java.nio.file.Files.readAllBytes(new File(DATA_DIRECTORY + FORMAT_FILENAME).toPath()));
                String originFormatContent = formatContent;

                // 데이터 파일에서 데이터 읽기, 각 라인을 배열로 변환
                List<String[]> dataLines = java.nio.file.Files.lines(Paths.get(DATA_DIRECTORY + DATA_FILENAME))
                        .map(line -> line.split(","))
                        .collect(Collectors.toList());

                // 정규 표현식을 사용하여 플레이스홀더 찾기
                Pattern dataPattern = Pattern.compile("\\{\\{.*?\\}\\}");

                // 데이터 라인 만큼 반복
                for (String[] data : dataLines) {
                    // 작업 취소 확인 로직
                    String cancelled = taskCancellation(taskId, sender, session);
                    if (cancelled != null) return cancelled;

                    formatContent = originFormatContent;
                    Matcher matcher = dataPattern.matcher(formatContent);
                    StringBuffer result = new StringBuffer();

                    int dataIndex = 0;
                    while (matcher.find() && dataIndex < data.length) {
                        matcher.appendReplacement(result, Matcher.quoteReplacement(data[dataIndex++]));
                    }
                    matcher.appendTail(result);
                    formatContent = result.toString();  // 업데이트된 결과로 템플릿 내용 갱신

                    TextMessage message = session.createTextMessage();

                    Date now = new Date();
                    Locale currentLocale = new Locale("KOREAN", "KOREA");
                    String pattern = "yyyyMMddHHmmss";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
                    String nowString = simpleDateFormat.format(now);

                    message.setText(
                            "{" +
                                "\"CREATE_TIMESTAMP\": \"" + nowString + "\"," +
                                "\"MESSAGE_ID\": \"" + activeMQRequestFileAndDataDto.getTcName() + "\"," +
                                "\"DATA_MAP\": {" +
                                    "\"" + nowString + "\":{" +
                                        formatContent +
                                    "}" +
                                "}" +
                            "}"
                    );

//                    log.info("message : " + prettyPrintUsingGlobalSetting(message.getText()));

                    // message send
                    sender.send(message);
                    Thread.sleep(activeMQRequestFileAndDataDto.getDelayTime());
                }

                // connection close;
                sender.close();
                session.close();

            } catch (JMSException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                taskCancellationLogic.removeTask(taskId);
            }

            return "success";
        });
    }
}
