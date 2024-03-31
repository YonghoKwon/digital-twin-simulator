package com.dt.activemqsimulator.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class ClonedTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {

        Map<String, String> callerThreadContext = MDC.getCopyOfContextMap();
        return () -> {
            MDC.setContextMap(callerThreadContext);
            task.run();
        };
    }
}
