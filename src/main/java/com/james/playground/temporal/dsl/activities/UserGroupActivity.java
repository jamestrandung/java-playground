package com.james.playground.temporal.dsl.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
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

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class UserGroupInput {
    private Long groupId;
    private Long userId;
  }
}
