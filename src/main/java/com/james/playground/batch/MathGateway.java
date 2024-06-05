package com.james.playground.batch;

import java.util.concurrent.CompletableFuture;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@MessagingGateway
public interface MathGateway {
  @Gateway(requestChannel = "singleChannel")
  CompletableFuture<Integer> multiplyByTwoSingle(
      @Payload Integer number
  );

  @Gateway(requestChannel = "aggregateChannel")
  CompletableFuture<Integer> multiplyByTwoAggregate(
      @Header("correlationId") int correlationId,
      @Payload Integer number
  );

  @Gateway(requestChannel = "sumChannel")
  CompletableFuture<Integer> sumAggregate(
      @Header("correlationId") int correlationId,
      @Payload Integer number
  );
}
