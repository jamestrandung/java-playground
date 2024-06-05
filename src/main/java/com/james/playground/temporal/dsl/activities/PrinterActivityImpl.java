package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = PrinterActivity.QUEUE_NAME)
public class PrinterActivityImpl implements PrinterActivity {
  @Override
  public DynamicActivityResult print(PrinterInput input) {
    log.info("Printing, user ID: {}, text: {}", input.getUserId(), input.getNode().getText());

    return DynamicActivityResult.builder()
        .nextNodeId(input.getNode().getNextNodeId())
        .build();
  }
}
