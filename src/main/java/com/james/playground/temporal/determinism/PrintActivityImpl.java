package com.james.playground.temporal.determinism;

import io.temporal.spring.boot.ActivityImpl;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = PrintActivity.QUEUE_NAME)
public class PrintActivityImpl implements PrintActivity {
  @Override
  public void print(List<String> strings) {
    log.info(strings.toString());
  }

  @Override
  public void echo(List<String> strings) {
    log.warn(strings.toString());
  }
}
