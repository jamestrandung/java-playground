package com.james.playground.controller.temporal;

import com.james.playground.temporal.properties.Properties;
import com.james.playground.temporal.properties.PropertyWorkflow;
import com.james.playground.temporal.properties.PropertyWorkflow.PropertyRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/property")
public class PropertyController {
  @Lazy
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/start")
  public void startChild() {
    PropertyWorkflow workflow = this.workflowClient.newWorkflowStub(
        PropertyWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(PropertyWorkflow.QUEUE_NAME)
            .setWorkflowId(PropertyWorkflow.class.getName())
            .build()
    );

    WorkflowClient.execute(
            workflow::execute,
            PropertyRequest.builder()
                .properties(
                    Properties.builder()
                        .raw(Map.of(
                            "userIds", "[12345]"
                        ))
                        .build()
                )
                .build()
        )
        .thenAccept(result -> {
          log.info("End result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("End failure: {}", ex.getMessage());
          return null;
        });
  }
}
