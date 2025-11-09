package com.james.playground.temporal.properties;

import io.temporal.spring.boot.WorkflowImpl;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@WorkflowImpl(taskQueues = PropertyWorkflow.QUEUE_NAME)
public class PropertyWorkflowImpl implements PropertyWorkflow {
  @Override
  public String execute(PropertyRequest request) {
    Set<Long> userIds = request.getProperties().getSet("userIds");
    log.info("user IDs: {}", userIds);
    userIds.forEach(userId -> log.info("user ID: {}, class: {}", userId, userId.getClass()));

    List<String> list = StreamEx.of(userIds)
        .map(Objects::toString)
        .toList();

    log.info("list: {}", list);
    return "";
  }
}
