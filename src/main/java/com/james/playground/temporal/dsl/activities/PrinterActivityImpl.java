package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = PrinterActivity.QUEUE_NAME)
public class PrinterActivityImpl implements PrinterActivity {
  @Override
  public DynamicActivityResult print(PrinterInput input) {
    ActivityExecutionContext context = Activity.getExecutionContext();
    log.info(
        "Printing, workflow ID: {}, user ID: {}, text: {}",
        context.getInfo().getWorkflowId(), input.getUserId(), input.getNode().getText()
    );

    return DynamicActivityResult.builder()
        .nextNodeId(input.getNode().getNextNodeId())
        .build();
  }
}
