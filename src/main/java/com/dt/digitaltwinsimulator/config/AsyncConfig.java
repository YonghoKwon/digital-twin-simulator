package com.dt.digitaltwinsimulator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 코어 스레드 풀 크기 설정
        executor.setMaxPoolSize(20); // 최대 스레드 풀 크기 설정
        executor.setQueueCapacity(500); // 작업 큐 용량 설정

//        executor.setTaskDecorator(new ClonedTaskDecorator());
        executor.setThreadNamePrefix("AsyncThread-"); // 스레드 이름 접두사 설정
        executor.setThreadGroupName("Async-group");

        // 작업이 완료된 후 스레드 풀이 종료될 때까지 대기할 시간 설정 (단위: 초)
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler(); // 추가
    }

    @Slf4j
    public static class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error( ex.getMessage(), ex );
        }
    }
}
