package com.james.playground.temporal.dsl.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActivityInterface
public interface UserActivity {
  String QUEUE_NAME = "UserTaskQueue";

  @ActivityMethod
  UserInfoOutput getTimezone(UserInfoInput input);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class UserInfoInput {
    private Long userId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class UserInfoOutput {
    private Long userId;
    private ZoneId timezone;
  }
}
