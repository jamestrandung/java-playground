package com.james.playground.batch;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockPolicy implements RejectedExecutionHandler {
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    log.info("Blocking task, thread: {}", Thread.currentThread().getName());

    if (executor.isShutdown()) {
      throw new RejectedExecutionException("Executor is shutdown");
    } else {
      log.info("Offering task & wait, thread: {}", Thread.currentThread().getName());
      try {
        executor.getQueue().offer(r, 500, TimeUnit.MILLISECONDS);
      } catch (Exception ex) {
        throw new RejectedExecutionException("Unexpected InterruptedException", ex);
      }
    }
  }
}
