package com.james.playground.controller.temporal;

import com.james.playground.temporal.child.ChildWorkflow;
import com.james.playground.temporal.child.ParentWorkflow;
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
@RequestMapping("/temporal/child")
public class ChildController {
  @Lazy
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/start")
  public void startChild() {
    ChildWorkflow workflow = this.workflowClient.newWorkflowStub(
        ChildWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(ChildWorkflow.QUEUE_NAME)
            .setWorkflowId(ChildWorkflow.class.getName())
            .build()
    );

    WorkflowClient.execute(workflow::doSomethingElse)
        .thenAccept(result -> {
          log.info("End result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("End failure: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/start-parent")
  public void startParent() {
    ParentWorkflow workflow = this.workflowClient.newWorkflowStub(
        ParentWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(ParentWorkflow.QUEUE_NAME)
            .setWorkflowId(ParentWorkflow.class.getName())
            .build()
    );

    WorkflowClient.execute(workflow::doSomething)
        .thenAccept(result -> {
          log.info("End result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("End failure: {}", ex.getMessage());
          return null;
        });
  }
}
