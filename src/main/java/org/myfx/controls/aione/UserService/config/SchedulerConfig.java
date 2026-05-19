package org.myfx.controls.aione.UserService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // 单线程执行，避免多任务同时清理导致IO/CPU飙升
        scheduler.setThreadNamePrefix("avatar-clean-"); // 线程名前缀，便于日志排查
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // 任务冲突时丢弃（反正3个月一次，下次会执行）
        return scheduler;
    }
}