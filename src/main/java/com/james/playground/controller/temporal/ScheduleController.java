package com.james.playground.controller.temporal;

import com.james.playground.temporal.dsl.dto.RecurringSchedule;
import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.scheduling.ScheduledMoneyTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/schedule")
public class ScheduleController {
  @Autowired
  private ScheduledMoneyTransfer scheduledMoneyTransfer;

  @PostMapping
  public void scheduleTransfer(@RequestBody ScheduledMoneyTransferDetails details) {
    this.scheduledMoneyTransfer.scheduleMoneyTransfer(details);
  }

  @PutMapping
  public void manageTransferSchedule(@RequestParam String action) {
    this.scheduledMoneyTransfer.manageMoneyTransferSchedule(action);
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduledMoneyTransferDetails {
    private RecurringSchedule schedule;
    private TransactionDetails transaction;
  }
}
