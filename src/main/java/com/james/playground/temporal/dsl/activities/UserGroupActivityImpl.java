package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.utils.MiscUtils;
import io.temporal.spring.boot.ActivityImpl;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = UserGroupActivity.QUEUE_NAME)
public class UserGroupActivityImpl implements UserGroupActivity {
  @Override
  public void addToGroup(UserGroupInput input) {
    log.info("Adding user {} to group {}", input.getUserId(), input.getGroupId());
    MiscUtils.sleep(Duration.ofSeconds(5));
    log.info("Added user {} to group {}", input.getUserId(), input.getGroupId());
  }

  @Override
  public void removeFromGroup(UserGroupInput input) {
    log.info("Removing user {} from group {}", input.getUserId(), input.getGroupId());
    MiscUtils.sleep(Duration.ofSeconds(5));
    log.info("Removed user {} from group {}", input.getUserId(), input.getGroupId());
  }
}
