package com.dt.activemqsimulator.logic;

import com.dt.activemqsimulator.dto.ActiveMQRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.apache.activemq.artemis.utils.RandomUtil.*;

@Service
public class ActiveMQRequestLogic {

    @Async
    public CompletableFuture<String> sendTopic(String flag, ActiveMQRequestDto activeMQRequestDto) {
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
                if(activeMQRequestDto.isRepeat()) {
                    // TODO: 반복시간만큼으로 변경 필요
                    while (true) {
                        TextMessage message = session.createTextMessage();

                        Date now = new Date();
                        Locale currentLocale = new Locale("KOREAN", "KOREA");
                        String pattern = "yyyyMMddHHmmss";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
                        String nowString = simpleDateFormat.format(now);

                        String[] keyArray = activeMQRequestDto.getFormat().get(0).get("dataId").split(",");
                        String[] keyTypeArray = activeMQRequestDto.getFormat().get(0).get("dataType").split(",");
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
                                        makeRandomData(keyArray, keyTypeArray, randomConditionArray) +
                                    "}" +
                                "}" +
                            "}"
                        );

                        System.out.println("message : " + message.getText());
                        System.out.println("message : " + prettyPrintUsingGlobalSetting(message.getText()));

                        // message send
                        sender.send(message);

                        Thread.sleep(activeMQRequestDto.getDelayTime());

                        // connection close;
                        sender.close();
                        session.close();
                    }
                }


                // message creates in value count
                for(int i = 0; i < (activeMQRequestDto.getValue().get(0)).size(); i++) {
                    TextMessage message = session.createTextMessage();

                    Date now = new Date();
                    Locale currentLocale = new Locale("KOREAN", "KOREA");
                    String pattern = "yyyyMMddHHmmss";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, currentLocale);
                    String nowString = simpleDateFormat.format(now);

                    String[] keyArray = activeMQRequestDto.getFormat().get(0).get("dataId").split(",");
                    String[] valueArray = activeMQRequestDto.getValue().get(0).get(i+1).split(",");

                    message.setText(
                        "{" +
                            "\"CREATE_TIMESTAMP\": \"" + nowString + "\"," +
                            "\"MESSAGE_ID\": \"" + activeMQRequestDto.getTcName() + "\"," +
                            "\"DATA_MAP\": {" +
                                "\"" + nowString + "\":{" +
                                    // Key: Value
                                    // make key and value in keyArray size
//                                    "\"" + keyArray[j] + "\": \"" + valueArray[j] + "\"," +
                                    makeData(keyArray, valueArray) +
                                "}" +
                            "}" +
                        "}"
                    );

                    System.out.println("message : " + message.getText());
                    System.out.println("message : " + prettyPrintUsingGlobalSetting(message.getText()));

                    // message send
                    sender.send(message);

                    Thread.sleep(activeMQRequestDto.getDelayTime());

                    // connection close;
                    sender.close();
                    session.close();
                }

            } catch (JMSException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

//            System.out.println("start flag = " + flag + " activeMQRequestDto = " + activeMQRequestDto);
//            for(int i = 0; i < 10; i++) {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("flag = " + flag + " i = " + i);
//            }
//
//            // delay time 만큼 sleep
//            try {
//                Thread.sleep(30000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("end flag = " + flag + " activeMQRequestDto = " + activeMQRequestDto);

            return "success";
        });
    }

    private String makeRandomData(String[] keyArray, String[] keyTypeArray, String[] randomConditionArray) {
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
        // i 길이의 랜덤 문자열 생성하여 리턴
        return RandomStringUtils.randomAlphanumeric(i);
    }

    private String randomInteger(int i) {
        // i 범위의 랜덤 정수 생성하여 리턴
        return String.valueOf(RandomUtil.randomInterval(0, i));
    }

    private String randomDouble(double v) {
        return null;
    }

    private String randomBoolean() {
        return null;
    }

    private String randomDate() {
        return null;
    }

    private String makeData(String[] keyArray, String[] valueArray) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < keyArray.length; i++) {
            if(i == keyArray.length - 1)
                sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\"");
            else
                sb.append("\"" + keyArray[i].trim() + "\": \"" + valueArray[i].trim() + "\",");
        }
        return sb.toString();
    }

    public String prettyPrintUsingGlobalSetting(String uglyJsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object jsonObject = mapper.readValue(uglyJsonString, Object.class);
        String prettyJson = mapper.writeValueAsString(jsonObject);
        return prettyJson;
    }
}
