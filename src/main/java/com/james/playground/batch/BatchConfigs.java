package com.james.playground.batch;

import io.grpc.netty.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableAsync
@Configuration
public class BatchConfigs {
  @Bean
  public ThreadPoolExecutor controllerExecutor() {
    ThreadFactory factory = new DefaultThreadFactory("batching-controller");
    return new ThreadPoolExecutor(1, 1, 300, TimeUnit.SECONDS, new SynchronousQueue<>(), factory, new BlockPolicy());
  }

  @Bean(name = IntegrationContextUtils.TASK_SCHEDULER_BEAN_NAME)
  public TaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.setThreadNamePrefix("my-task-scheduler-");
    scheduler.setRejectedExecutionHandler(new CallerRunsPolicy());
    scheduler.initialize(); // Must initialize before use
    return scheduler;
  }

  @Bean(name = "batchingTaskExecutor")
  public ThreadPoolExecutor taskExecutor() {
    ThreadFactory factory = new DefaultThreadFactory("batching-task-executor");
    return new ThreadPoolExecutor(16, 16, 300, TimeUnit.SECONDS, new SynchronousQueue<>(), factory, new BlockPolicy());
  }
}
