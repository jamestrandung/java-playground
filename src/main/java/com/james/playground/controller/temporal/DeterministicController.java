package com.james.playground.controller.temporal;

import com.james.playground.temporal.determinism.DeterministicWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/deterministic")
public class DeterministicController {
  @Lazy
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/start")
  public void startChild() {
    DeterministicWorkflow workflow = this.workflowClient.newWorkflowStub(
        DeterministicWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(DeterministicWorkflow.QUEUE_NAME)
            .setWorkflowId(DeterministicWorkflow.class.getName())
            .build()
    );

    WorkflowClient.execute(workflow::execute)
        .thenAccept(result -> {
          log.info("End result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("End failure: {}", ex.getMessage());
          return null;
        });
  }
}
