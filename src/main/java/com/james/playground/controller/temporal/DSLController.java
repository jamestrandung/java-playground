package com.james.playground.controller.temporal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.james.playground.temporal.dsl.activities.UserGroupActivity;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayInterruptionCategory;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayNode;
import com.james.playground.temporal.dsl.workflows.marketing.MarketingVersionChangeWorkflow;
import com.james.playground.temporal.dsl.workflows.marketing.MarketingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
  private MarketingWorkflowStore marketingWorkflowStore;
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
        .handleChangeSignals(
            null,
            List.of(
                DelayInterruptionSignal.builder()
                    .category(DelayInterruptionCategory.IMMEDIATE_RELEASE)
                    .affectedNodeId(affectedNodeId)
                    .build()
            )
        );
  }

  @PostMapping("/delay/simulation/removal")
  public void simulateDelayRemoval(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId
  ) {
    this.marketingWorkflowStore.findWorkflowDefinition(workflowDefinitionId)
        .findNodeAcceptingDeletedNode(affectedNodeId)
        .setDeletedOn(System.currentTimeMillis());

    this.workflowClient.newWorkflowStub(
            MarketingWorkflow.class,
            String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
        )
        .handleChangeSignals(
            null,
            List.of(
                DelayInterruptionSignal.builder()
                    .category(DelayInterruptionCategory.DURATION_MODIFIED)
                    .affectedNodeId(affectedNodeId)
                    .build()
            )
        );
  }

  @PostMapping("/delay/simulation/duration-change")
  public void simulateDelayDurationChange(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId,
      @RequestParam Integer durationInSeconds
  ) {
    WorkflowNode node = this.marketingWorkflowStore.findWorkflowDefinition(workflowDefinitionId)
        .findNodeAcceptingDeletedNode(affectedNodeId);

    if (node instanceof DelayNode delayNode) {
      delayNode.setDurationInSeconds(durationInSeconds);

      this.workflowClient.newWorkflowStub(
              MarketingWorkflow.class,
              String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
          )
          .handleChangeSignals(
              null,
              List.of(
                  DelayInterruptionSignal.builder()
                      .category(DelayInterruptionCategory.DURATION_MODIFIED)
                      .affectedNodeId(affectedNodeId)
                      .build()
              )
          );
    }
  }

  @PostMapping("/delay/simulation/next-node-change")
  public void simulateDelayDurationChange(
      @RequestParam String workflowDefinitionId,
      @RequestParam Long userId,
      @RequestParam String affectedNodeId
  ) {
    WorkflowNode node = this.marketingWorkflowStore.findWorkflowDefinition(workflowDefinitionId)
        .findNodeAcceptingDeletedNode(affectedNodeId);

    PrinterNode newNode = PrinterNode.builder()
        .id("999")
        .nextNodeId(node.getNextNodeId())
        .text("Node inserted after delay")
        .build();

    node.setNextNodeId(newNode.getId());

    this.marketingWorkflowStore.findWorkflowDefinition(workflowDefinitionId)
        .put(newNode.getId(), newNode);

    this.workflowClient.newWorkflowStub(
            MarketingWorkflow.class,
            String.format(MarketingWorkflow.WORKFLOW_ID_FORMAT, workflowDefinitionId, userId)
        )
        .handleChangeSignals(
            null,
            List.of(
                DelayInterruptionSignal.builder()
                    .category(DelayInterruptionCategory.CONFIG_MODIFIED)
                    .affectedNodeId(affectedNodeId)
                    .build()
            )
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

  @GetMapping("/jackson-serialization")
  public void jacksonSerialization() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    log.info("JSON obj: {}", mapper.writeValueAsString(
        DelayInterruptionSignal.builder()
            .category(DelayInterruptionCategory.IMMEDIATE_RELEASE)
            .affectedNodeId("123")
            .build()
    ));

    log.info("JSON list: {}", mapper.writeValueAsString(
        List.of(
            DelayInterruptionSignal.builder()
                .category(DelayInterruptionCategory.IMMEDIATE_RELEASE)
                .affectedNodeId("123")
                .build()
        )
    ));
  }

  @PostMapping("/version-change")
  public void detectChangesAndBroadcastSignals(
      @RequestParam String workflowDefinitionId,
      @RequestParam String newVersionName
  ) throws IOException {
    MarketingVersionChangeWorkflow workflow = this.workflowClient.newWorkflowStub(
        MarketingVersionChangeWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MarketingVersionChangeWorkflow.QUEUE_NAME)
            .setWorkflowId(MarketingVersionChangeWorkflow.WORKFLOW_ID_PREFIX + workflowDefinitionId)
            .build()
    );

    Resource resource = new ClassPathResource(newVersionName);

    MarketingWorkflowDefinition old = this.marketingWorkflowStore.findWorkflowDefinition(workflowDefinitionId);
    MarketingWorkflowDefinition latest = MarketingWorkflowStore.OBJECT_MAPPER.readValue(
        resource.getInputStream(),
        MarketingWorkflowDefinition.class
    );

    this.marketingWorkflowStore.updateWorkflowDefinition(workflowDefinitionId, latest);

    WorkflowClient.execute(workflow::detectChangesAndBroadcastSignals, old, latest)
        .exceptionally(ex -> {
          log.error("Marketing version change workflow failure: {}", ex.getMessage());
          return null;
        });
  }
}
