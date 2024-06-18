package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActivityInterface
public interface UserGroupActivity {
  String QUEUE_NAME = "UserGroupTaskQueue";

  @ActivityMethod
  void addToGroup(UserGroupInput input);

  @ActivityMethod
  void removeFromGroup(UserGroupInput input);

  @ActivityMethod
  GetUserIdsInGroupOutput getUserIdsInGroup(GetUserIdsInGroupInput input);

  @ActivityMethod
  <T> void broadcastSignalToGroup(GroupSignalInput input);

  @ActivityMethod
  void updateInMemoryCounter(UserGroupInput input);

  Map<Long, AtomicInteger> getInMemoryCounters();

  void resetInMemoryCounters();

  @ActivityMethod
  boolean isUserInGroup(UserGroupInput input);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class UserGroupInput {
    private Long groupId;
    private Long userId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class GetUserIdsInGroupInput {
    private Long groupId;
    private Long partitionId;
    private int pageIdx;
    private int pageSize;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class GetUserIdsInGroupOutput {
    private Long groupId;
    private Long partitionId;
    private int pageIdx;
    private int pageSize;
    private boolean hasNext;
    private List<Long> userIds;
  }
}
