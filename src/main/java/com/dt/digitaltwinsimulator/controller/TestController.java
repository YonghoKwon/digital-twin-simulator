package com.dt.digitaltwinsimulator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() throws IOException {
        // local file read
        String DATA_DIRECTORY = "c:/Project/";
        String formatFileName = "KE2D1Z11_format.txt";
        String dataFileName = "KE2D1Z11.txt";

//        String fileContents = new String(java.nio.file.Files.readAllBytes(new File(DATA_DIRECTORY + formatFileName).toPath()));

        // 템플릿 파일에서 내용 읽기
        String templateContent = new String(java.nio.file.Files.readAllBytes(new File(DATA_DIRECTORY + formatFileName).toPath()));
        String originTemplateContent = templateContent;

        // 데이터 파일에서 데이터 읽기, 각 라인을 배열로 변환
        List<String[]> dataLines = Files.lines(Paths.get(DATA_DIRECTORY + dataFileName))
                .map(line -> line.split(","))
                .collect(Collectors.toList());

        // 정규 표현식을 사용하여 플레이스홀더 찾기
        Pattern pattern = Pattern.compile("\\{\\{.*?\\}\\}");

        // 각 데이터 라인마다 플레이스홀더 치환
        for (String[] data : dataLines) {
            templateContent = originTemplateContent;
            Matcher matcher = pattern.matcher(templateContent);  // 매 라운드마다 새로운 Matcher 생성
            StringBuffer result = new StringBuffer();
            int dataIndex = 0;
            while (matcher.find() && dataIndex < data.length) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(data[dataIndex++]));
            }
            matcher.appendTail(result);
            templateContent = result.toString();  // 업데이트된 결과로 템플릿 내용 갱신

            // 최종 결과 출력
            System.out.println(templateContent);
        }

        return "Hello World!";
    }







    private Map<String, String> parseFormat(List<String> lines) {
        Map<String, String> formatMap = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split(":");
            formatMap.put(parts[0].trim(), parts[1].trim());
        }
        return formatMap;
    }

    private Map<String, String> mapData(Map<String, String> formatMap, List<String> dataLines) {
        Map<String, String> dataMap = new HashMap<>();
        for (String line : dataLines) {
            String[] values = line.split(", ");
            for (Map.Entry<String, String> entry : formatMap.entrySet()) {
                int index = Integer.parseInt(entry.getValue().replace("{{format", "").replace("}}", "")) - 1;
                dataMap.put(entry.getKey(), values[index]);
            }
        }
        return dataMap;
    }
}
