package com.james.playground.temporal.scheduling;

import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.moneytransfer.workflows.MoneyTransferWorkflow;
import io.temporal.api.enums.v1.ScheduleOverlapPolicy;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.Schedule;
import io.temporal.client.schedules.ScheduleActionStartWorkflow;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleHandle;
import io.temporal.client.schedules.ScheduleIntervalSpec;
import io.temporal.client.schedules.ScheduleOptions;
import io.temporal.client.schedules.SchedulePolicy;
import io.temporal.client.schedules.ScheduleSpec;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMoneyTransfer {
  private static final String SCHEDULE_NAME = "ScheduledMoneyTransfer";

  @Autowired
  private ScheduleClient scheduleClient;

  public void scheduleMoneyTransfer(TransactionDetails details) {
    ScheduleSpec specs = ScheduleSpec.newBuilder()
        .setIntervals(List.of(new ScheduleIntervalSpec(Duration.ofSeconds(2))))
        .build();

    ScheduleActionStartWorkflow action = ScheduleActionStartWorkflow.newBuilder()
        .setWorkflowType(MoneyTransferWorkflow.class)
        .setArguments(details)
        .setOptions(
            WorkflowOptions.newBuilder()
                .setTaskQueue(MoneyTransferWorkflow.QUEUE_NAME)
                .setWorkflowId("money-transfer-workflow") // Actual ID sample: money-transfer-workflow-2024-05-26T12:22:02Z
                // Temporal will throw errors instead of ignoring when duplicate workflow ID is used,
                // regardless of state (running, completed, failed, or timed out)
                //                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                .build()
        )
        .build();

    SchedulePolicy policy = SchedulePolicy.newBuilder()
        .setPauseOnFailure(true) // ONLY work if ScheduleOverlapPolicy is NOT SCHEDULE_OVERLAP_POLICY_ALLOW_ALL
        .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL)
        .build();

    Schedule schedule = Schedule.newBuilder()
        .setSpec(specs)
        .setAction(action)
        .setPolicy(policy)
        .build();

    ScheduleOptions options = ScheduleOptions.newBuilder()
        .setTriggerImmediately(true)
        .build();

    scheduleClient.createSchedule(SCHEDULE_NAME, schedule, options);
  }

  public void manageMoneyTransferSchedule(String action) {
    ScheduleHandle handle = scheduleClient.getHandle(SCHEDULE_NAME);

    switch (action) {
      case "pause":
        handle.pause();
        break;
      case "resume":
        handle.unpause();
        break;
      case "terminate":
        handle.delete();
        break;
      default:
        throw new IllegalArgumentException("Invalid action: " + action);
    }
  }
}
