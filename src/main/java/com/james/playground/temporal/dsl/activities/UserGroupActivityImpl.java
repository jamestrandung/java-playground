package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import com.james.playground.temporal.utils.MiscUtils;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.spring.boot.ActivityImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = UserGroupActivity.QUEUE_NAME)
public class UserGroupActivityImpl implements UserGroupActivity {
  private final Map<Long, AtomicInteger> counters = new ConcurrentHashMap<>();
  @Autowired
  private WorkflowClient workflowClient;

  @Override
  public void addToGroup(UserGroupInput input) {
    log.info("Adding user {} to group {}", input.getUserId(), input.getGroupId());
    MiscUtils.sleep(Duration.ofSeconds(2));
    log.info("Added user {} to group {}", input.getUserId(), input.getGroupId());
  }

  @Override
  public void removeFromGroup(UserGroupInput input) {
    log.info("Removing user {} from group {}", input.getUserId(), input.getGroupId());
    MiscUtils.sleep(Duration.ofSeconds(2));
    log.info("Removed user {} from group {}", input.getUserId(), input.getGroupId());
  }

  @Override
  public GetUserIdsInGroupOutput getUserIdsInGroup(GetUserIdsInGroupInput input) {
    List<Long> userIds = new ArrayList<>();

    int startIdx = input.getPageSize() * input.getPageIdx();
    int endIdx   = input.getPageSize() * (input.getPageIdx() + 1) + input.getPageIdx();

    for (long i = startIdx; i < endIdx; i++) {
      userIds.add(i);
    }

    return GetUserIdsInGroupOutput.builder()
        .groupId(input.getGroupId())
        .partitionId(input.getPartitionId())
        .pageIdx(input.getPageIdx())
        .pageSize(input.getPageSize())
        .hasNext(input.getPageIdx() < 1)
        .userIds(userIds)
        .build();
  }

  @Override
  public <T> void broadcastSignalToGroup(GroupSignalInput<T> input) {
    log.info("Started broadcasting Signal from Activity");

    ActivityExecutionContext context = Activity.getExecutionContext();

    int pageIdx = context.getHeartbeatDetails(Integer.class)
        .orElse(0);

    while (true) {
      log.info("Working on page index: {}", pageIdx);

      GetUserIdsInGroupOutput output = this.getUserIdsInGroup(
          GetUserIdsInGroupInput.builder()
              .groupId(input.getGroupId())
              .partitionId(input.getGroupId())
              .pageIdx(pageIdx)
              .pageSize(input.getPageSize())
              .build()
      );

      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (Long userId : output.getUserIds()) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
          try {
            WorkflowStub workflowStub = this.workflowClient.newUntypedWorkflowStub(input.getWorkflowIdPrefix() + userId);
            workflowStub.signal(input.getSignalName(), input.getPayload());

          } catch (Exception ex) {
            log.info(
                "Failed to send Signal, user ID: {}, workflow ID: {}, error: {}",
                userId, input.getWorkflowIdPrefix() + userId, ex.getMessage()
            );
          }
        });

        futures.add(future);
      }

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

      if (!output.isHasNext()) {
        break;
      }

      pageIdx += 1;
      context.heartbeat(pageIdx);
    }

    log.info("Completed broadcasting Signal from Activity");
  }

  @Override
  public void updateInMemoryCounter(UserGroupInput input) {
    this.counters.compute(input.getGroupId(), (k, v) -> {
      if (v == null) {
        return new AtomicInteger(1);
      }

      v.incrementAndGet();
      return v;
    });
  }

  @Override
  public Map<Long, AtomicInteger> getInMemoryCounters() {
    return this.counters;
  }

  @Override
  public void resetInMemoryCounters() {
    this.counters.clear();
  }

  @Override
  public boolean isUserInGroup(UserGroupInput input) {
    // Fetch all users from the given group page by page and
    // save the results into Redis using LinkedHashSet by page
    // as well. The goal is to reuse the same results in different
    // workflows.
    return input.getGroupId() > input.getUserId();
  }
}
