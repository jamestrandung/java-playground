package com.james.playground.controller.temporal;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.MarketingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/dsl")
public class DSLController {
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/parse")
  public void parse(@RequestBody Map<String, WorkflowNode> dsl) {
    log.info("DSL: {}", dsl);
  }

  @PostMapping
  public void startMarketingWorkflow(@RequestParam String workflowDefinitionId, @RequestParam Long userId) {
    MarketingWorkflow workflow = this.workflowClient.newWorkflowStub(
        MarketingWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MarketingWorkflow.QUEUE_NAME)
            .setWorkflowId(String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId))
            .build()
    );

    WorkflowClient.execute(
            workflow::execute,
            DynamicWorkflowInput.builder()
                .workflowDefinitionId(workflowDefinitionId)
                .userId(userId)
                .build()
        )
        .exceptionally(ex -> {
          log.error("Marketing workflow failure: {}", ex.getMessage());
          return null;
        });
  }
}
