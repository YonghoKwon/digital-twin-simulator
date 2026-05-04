package com.dt.digitaltwinsimulator.controller;

import org.springframework.context.annotation.Profile;
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

@Profile({"local", "dev"})
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() throws IOException {
        String DATA_DIRECTORY = "c:/Project/";
        String formatFileName = "KE2D1Z11_format.txt";
        String dataFileName = "KE2D1Z11.txt";

        String templateContent = new String(java.nio.file.Files.readAllBytes(new File(DATA_DIRECTORY + formatFileName).toPath()));
        String originTemplateContent = templateContent;

        List<String[]> dataLines = Files.lines(Paths.get(DATA_DIRECTORY + dataFileName))
                .map(line -> line.split(","))
                .collect(Collectors.toList());

        Pattern pattern = Pattern.compile("\\{\\{.*?\\}\\}");

        for (String[] data : dataLines) {
            templateContent = originTemplateContent;
            Matcher matcher = pattern.matcher(templateContent);
            StringBuffer result = new StringBuffer();
            int dataIndex = 0;
            while (matcher.find() && dataIndex < data.length) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(data[dataIndex++]));
            }
            matcher.appendTail(result);
            templateContent = result.toString();
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
