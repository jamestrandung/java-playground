package com.james.playground.temporal.dsl.activities;

import io.temporal.spring.boot.ActivityImpl;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = UserActivity.QUEUE_NAME)
public class UserActivityImpl implements UserActivity {
  @Override
  public UserInfoOutput getTimezone(UserInfoInput input) {
    log.info("Fetching timezone for user ID: {}", input.getUserId());

    return UserInfoOutput.builder()
        .userId(input.getUserId())
        .timezone(ZoneOffset.ofHours(7))
        .build();
  }
}
