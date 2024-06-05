package com.james.playground.controller.temporal;

import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.scheduling.ScheduledMoneyTransfer;
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
  public void scheduleTransfer(@RequestBody TransactionDetails details) {
    scheduledMoneyTransfer.scheduleMoneyTransfer(details);
  }

  @PutMapping
  public void manageTransferSchedule(@RequestParam String action) {
    scheduledMoneyTransfer.manageMoneyTransferSchedule(action);
  }
}
