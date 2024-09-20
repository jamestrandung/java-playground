package com.james.playground.controller;

import com.james.playground.batch.MathGateway;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/batch")
public class BatchController {
  @Autowired
  MathGateway mathGateway;

  @PostMapping("/single")
  public String batch(@RequestParam int limit) {
    log.info("TestController.batch single start");

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < limit; i++) {
      int input = i;

      CompletableFuture<Void> future = this.mathGateway.multiplyByTwoSingle(input)
          .thenAccept(result -> {
            log.info("TestController.batch single result, input: {}, output: {}", input, result);
          })
          .exceptionally(throwable -> {
            log.info("TestController.batch single failed, input: {}, error: {}", input, throwable.getMessage());

            return null;
          });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("TestController.batch single finished");

    return "DONE";
  }

  @PostMapping("/multiply/aggregate")
  public String multiplyAggregate(@RequestParam int limit, @RequestParam(required = false) Integer correlationId) {
    log.info("TestController.multiplyAggregate start");

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < limit; i++) {
      int input              = i;
      int finalCorrelationId = correlationId == null ? i % 2 : correlationId;

      CompletableFuture<Void> future = this.mathGateway.multiplyByTwoAggregate(finalCorrelationId, input)
          .thenAccept(result -> {
            log.info("TestController.multiplyAggregate result, input: {}, output: {}", input, result.orElse(null));
          })
          .exceptionally(throwable -> {
            log.info("TestController.multiplyAggregate failed, input: {}, error: {}", input, throwable.getMessage());

            return null;
          });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("TestController.multiplyAggregate finished");

    return "DONE";
  }

  @PostMapping("/sum/aggregate")
  public String sumAggregate(@RequestParam int limit, @RequestParam(required = false) Integer correlationId) {
    log.info("TestController.sumAggregate start");

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < limit; i++) {
      int input              = i;
      int finalCorrelationId = correlationId == null ? i % 2 : correlationId;

      CompletableFuture<Void> future = this.mathGateway.sumAggregate(finalCorrelationId, input)
          .thenAccept(result -> {
            log.info("TestController.sumAggregate result, input: {}, output: {}", input, result.orElse(null));
          })
          .exceptionally(throwable -> {
            log.info("TestController.sumAggregate failed, input: {}, error: {}", input, throwable.getMessage());

            return null;
          });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("TestController.sumAggregate finished");

    return "DONE";
  }

  @PostMapping("/echo/aggregate")
  public String echoAggregate(@RequestParam int limit, @RequestParam(required = false) Integer correlationId) {
    log.info("TestController.echoAggregate start");

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < limit; i++) {
      int input              = i;
      int finalCorrelationId = correlationId == null ? i % 2 : correlationId;

      CompletableFuture<Void> future = this.mathGateway.echoAggregate(finalCorrelationId, input)
          .thenAccept(result -> {
            log.info("TestController.echoAggregate result, input: {}, output: {}", input, result);
          })
          .exceptionally(throwable -> {
            log.info("TestController.echoAggregate failed, input: {}, error: {}", input, throwable.getMessage());

            return null;
          });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("TestController.echoAggregate finished");

    return "DONE";
  }
}
