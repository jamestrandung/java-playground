package com.james.playground.controller.temporal;

import com.james.playground.temporal.dsl.activities.UserGroupActivity;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.DelayNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionType;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import com.james.playground.temporal.dsl.workflows.MarketingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/dsl")
public class DSLController {
  @Autowired
  private WorkflowClient workflowClient;
  @Autowired
  private WorkflowStore workflowStore;
  @Autowired
  private UserGroupActivity userGroupActivity;

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

  @PostMapping("/multiple")
  public void startMultipleMarketingWorkflows(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam Integer count
  ) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      MarketingWorkflow workflow = this.workflowClient.newWorkflowStub(
          MarketingWorkflow.class,
          WorkflowOptions.newBuilder()
              .setTaskQueue(MarketingWorkflow.QUEUE_NAME)
              .setWorkflowId(String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId) + "-" + i)
              .build()
      );

      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        WorkflowClient.start(
            workflow::execute,
            DynamicWorkflowInput.builder()
                .workflowDefinitionId(workflowDefinitionId)
                .userId(userId)
                .build()
        );
      });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }

  @PostMapping("/delay/release")
  public void releaseUsersFromDelayImmediately(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId
  ) {
    this.workflowClient.newWorkflowStub(
            MarketingWorkflow.class,
            String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
        )
        .interruptDelay(
            DelayInterruptionSignal.builder()
                .type(DelayInterruptionType.IMMEDIATE_RELEASE)
                .affectedNodeId(affectedNodeId)
                .build()
        );
  }

  @PostMapping("/delay/simulation/removal")
  public void simulateDelayRemoval(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId
  ) {
    this.workflowStore.findNodeAcceptingDeletedNode(workflowDefinitionId, affectedNodeId)
        .setDeletedOn(System.currentTimeMillis());

    this.workflowClient.newWorkflowStub(
            MarketingWorkflow.class,
            String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
        )
        .interruptDelay(
            DelayInterruptionSignal.builder()
                .type(DelayInterruptionType.DURATION_MODIFIED)
                .affectedNodeId(affectedNodeId)
                .build()
        );
  }

  @PostMapping("/delay/simulation/duration-change")
  public void simulateDelayDurationChange(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId,
      @RequestParam Integer durationInSeconds
  ) {
    WorkflowNode node = this.workflowStore.findNodeAcceptingDeletedNode(workflowDefinitionId, affectedNodeId);

    if (node instanceof DelayNode delayNode) {
      delayNode.setDurationInSeconds(durationInSeconds);

      this.workflowClient.newWorkflowStub(
              MarketingWorkflow.class,
              String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
          )
          .interruptDelay(
              DelayInterruptionSignal.builder()
                  .type(DelayInterruptionType.DURATION_MODIFIED)
                  .affectedNodeId(affectedNodeId)
                  .build()
          );
    }
  }

  @PostMapping("/delay/simulation/next-node-change")
  public void simulateDelayDurationChange(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId
  ) {
    WorkflowNode node = this.workflowStore.findNodeAcceptingDeletedNode(workflowDefinitionId, affectedNodeId);

    PrinterNode newNode = PrinterNode.builder()
        .id("999")
        .nextNodeId(node.getNextNodeId())
        .text("Node inserted after delay")
        .build();

    node.setNextNodeId(newNode.getId());

    this.workflowStore.findWorkflowDefinition(workflowDefinitionId)
        .put(newNode.getId(), newNode);

    this.workflowClient.newWorkflowStub(
            MarketingWorkflow.class,
            String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
        )
        .interruptDelay(
            DelayInterruptionSignal.builder()
                .type(DelayInterruptionType.CONFIG_MODIFIED)
                .affectedNodeId(affectedNodeId)
                .build()
        );
  }

  @GetMapping("/counters")
  public Map<Long, AtomicInteger> getInMemoryCounters() {
    return this.userGroupActivity.getInMemoryCounters();
  }

  @DeleteMapping("/counters")
  public void resetInMemoryCounters() {
    this.userGroupActivity.resetInMemoryCounters();
  }
}
