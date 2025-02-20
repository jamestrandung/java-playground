package com.james.playground.batch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy;
import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@EnableIntegration
@IntegrationComponentScan("com.james.playground")
public class IntegrationConfig {
  @Autowired
  MathService mathService;

  @Bean
  public MessageChannel singleChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel aggregateChannel() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5); // Minimum threads
    executor.setMaxPoolSize(10); // Maximum threads
    executor.setQueueCapacity(25); // Queue size before adding more threads
    executor.setThreadNamePrefix("multiplication-aggregate-");
    executor.initialize(); // Must initialize before use

    return new ExecutorChannel(executor);

    //    return new DirectChannel();
  }

  @Bean
  public MessageChannel sumChannel() {
    //    ThreadFactory      factory  = new DefaultThreadFactory("sum-aggregate");
    //    ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), factory);
    //
    //    return new ExecutorChannel(executor);

    return new DirectChannel();
  }

  @Bean
  public MessageChannel echoChannel() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow processMultiplicationSingle() {
    /* WORKING IN BOTH HAPPY AND EXCEPTION PATHS */

    return IntegrationFlows.from(this.singleChannel())
        .handle((payload, headers) -> this.mathService.multiplyByTwo((Integer) payload))
        .get();

    /* NOT WORKING */

    //    return IntegrationFlows.from(this.singleChannel())
    //        .channel(c -> c.executor(Executors.newCachedThreadPool())) // Handle concurrent requests
    //        .publishSubscribeChannel(s -> s
    //            .subscribe(subFlow -> subFlow
    //                .handle((payload, headers) -> ((Integer) payload) * 2)
    //                .channel(MessageChannels.queue("replyChannel")))
    //        )
    //        .get();
  }

  @Bean
  public IntegrationFlow processMultiplicationAggregate() {
    /* WORKING IN BOTH HAPPY AND EXCEPTION PATHS */

    return IntegrationFlows.from("aggregateChannel")
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processMultiplication thread name 1: {}", Thread.currentThread().getName());

          return null;
        })
        .aggregate(aggregator -> aggregator
            .messageStore(new SimpleMessageStore()) // Infinite capacity
            .correlationStrategy(new HeaderAttributeCorrelationStrategy("correlationId"))
            .releaseStrategy(new MessageCountReleaseStrategy(5)) // Max batch size
            .groupTimeout(1000)  // Timeout for releasing the batch
            .expireGroupsUponCompletion(true)
            .expireGroupsUponTimeout(true)
            .sendPartialResultOnExpiry(true)
            .outputProcessor(group -> {
              log.info("IntegrationConfig.processMultiplication message count: {}", CollectionUtils.size(group.getMessages()));

              try {
                List<Integer> inputs = StreamEx.of(group.getMessages())
                    .map(message -> (Integer) message.getPayload())
                    .toList();

                Map<Integer, Integer> results = this.mathService.multiplyListByTwo(inputs);

                List<Pair<Object, Integer>> payload = StreamEx.of(group.getMessages())
                    .map(message ->
                             Pair.of(
                                 message.getHeaders().getReplyChannel(),
                                 results.get((Integer) message.getPayload())
                             )
                    ).toList();

                return MessageBuilder.withPayload(payload).build();

              } catch (Exception ex) {
                log.info("IntegrationConfig.processMultiplication grouping exception: {}", CollectionUtils.size(group.getMessages()));

                List<Pair<Object, Exception>> payload = StreamEx.of(group.getMessages())
                    .map(message -> Pair.of(message.getHeaders().getReplyChannel(), ex))
                    .toList();

                return MessageBuilder.withPayload(payload).build();
              }
            })
        )
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processMultiplication thread name 2: {}", Thread.currentThread().getName());

          return null;
        })
        .split()
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processMultiplication thread name 3: {}", Thread.currentThread().getName());

          return null;
        })
        .handle(Pair.class, (payload, headers) -> {
          Object result = (payload.getRight() instanceof Throwable) ? payload.getRight() : Optional.ofNullable(payload.getRight());

          return MessageBuilder.withPayload(result)
              .copyHeaders(headers)
              .setReplyChannel((MessageChannel) payload.getLeft())
              .setErrorChannel((MessageChannel) payload.getLeft())
              .build();
        })
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processMultiplication thread name 4: {}", Thread.currentThread().getName());

          return null;
        })
        .handle((payload, headers) -> payload)
        .get();
  }

  @Bean
  public IntegrationFlow processSumAggregate() {
    /* WORKING IN HAPPY PATH ONLY */

    //    return IntegrationFlows.from(this.sumChannel())
    //        .log()
    //        .aggregate(aggregator -> aggregator
    //            .messageStore(new SimpleMessageStore()) // Infinite capacity
    //        .correlationStrategy(new HeaderAttributeCorrelationStrategy("correlationId"))
    //        .releaseStrategy(new MessageCountReleaseStrategy(5)) // Max batch size
    //        .groupTimeout(1000)  // Timeout for releasing the batch
    //        .expireGroupsUponCompletion(true)
    //        .expireGroupsUponTimeout(true)
    //        .sendPartialResultOnExpiry(true)
    //            .outputProcessor(group -> {
    //              log.info("IntegrationConfig.processSumAggregate message count: {}", CollectionUtils.size(group.getMessages()));
    //
    //              List<Pair<Object, Integer>> payload = group.getMessages()
    //                  .stream()
    //                  .map(message -> Pair.of(message.getHeaders().getReplyChannel(), (Integer) message.getPayload()))
    //                  .collect(Collectors.toList());
    //
    //              return MessageBuilder.withPayload(payload).build();
    //            })
    //        )
    //        .split(new AbstractMessageSplitter() {
    //          @Override
    //          protected Object splitMessage(Message<?> message) {
    //            List<Pair<Object, Integer>> aggregatedPayloads = (List<Pair<Object, Integer>>) message.getPayload();
    //
    //            int total = StreamEx.of(aggregatedPayloads)
    //                .map(Pair::getRight)
    //                .toListAndThen(mathService::sumExceptionally);
    //
    //            return StreamEx.of(aggregatedPayloads)
    //                .map(payload -> MessageBuilder.withPayload(total)
    //                    .setReplyChannel((MessageChannel) payload.getLeft())
    //                    .setErrorChannel((MessageChannel) payload.getLeft())
    //                    .build())
    //                .toList();
    //          }
    //        })
    //        .log()
    //        .handle((payload, headers) -> payload)
    //        .get();

    /* WORKING IN BOTH HAPPY AND EXCEPTION PATHS */
    //    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //    executor.setCorePoolSize(5); // Minimum threads
    //    executor.setMaxPoolSize(10); // Maximum threads
    //    executor.setQueueCapacity(25); // Queue size before adding more threads
    //    executor.setThreadNamePrefix("default-task-executor-");
    //    executor.initialize(); // Must initialize before use

    return IntegrationFlows.from("sumChannel")
        //        .channel(MessageChannels.executor(executor)) // NOT WORKING
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processSumAggregate thread name: {}", Thread.currentThread().getName());

          return null;
        })
        .aggregate(aggregator -> aggregator
            .messageStore(new SimpleMessageStore()) // Infinite capacity
            .correlationStrategy(new HeaderAttributeCorrelationStrategy("correlationId"))
            .releaseStrategy(new MessageCountReleaseStrategy(5)) // Max batch size
            .groupTimeout(1000)  // Timeout for releasing the batch
            .expireGroupsUponCompletion(true)
            .expireGroupsUponTimeout(true)
            .sendPartialResultOnExpiry(true)
            //            .async(true)
            .outputProcessor(group -> {
              log.info("IntegrationConfig.processSumAggregate message count: {}", CollectionUtils.size(group.getMessages()));

              try {
                Integer sum = StreamEx.of(group.getMessages())
                    .map(message -> (Integer) message.getPayload())
                    .toListAndThen(this.mathService::sum);

                List<Pair<Object, Integer>> payload = group.getMessages()
                    .stream()
                    .map(message -> Pair.of(message.getHeaders().getReplyChannel(), sum))
                    .collect(Collectors.toList());

                return MessageBuilder.withPayload(payload).build();

              } catch (Exception ex) {
                log.info("IntegrationConfig.processSumAggregate grouping exception: {}", CollectionUtils.size(group.getMessages()));

                List<Pair<Object, Exception>> payload = StreamEx.of(group.getMessages())
                    .map(message -> Pair.of(message.getHeaders().getReplyChannel(), ex))
                    .toList();

                return MessageBuilder.withPayload(payload).build();
              }
            })
            .requiresReply(true)
        )
        .log()
        .split()
        .log()
        .handle(Pair.class, (payload, headers) -> {
          Object result = (payload.getRight() instanceof Throwable) ? payload.getRight() : Optional.ofNullable(payload.getRight());

          return MessageBuilder.withPayload(result)
              .copyHeaders(headers)
              .setReplyChannel((MessageChannel) payload.getLeft())
              .setErrorChannel((MessageChannel) payload.getLeft())
              .build();
        })
        .log()
        .log(msg -> {
          log.info("IntegrationConfig.processSumAggregate thread name 2: {}", Thread.currentThread().getName());

          return null;
        })
        .handle((payload, headers) -> payload)
        .get();
  }

  @Bean
  public IntegrationFlow processEchoAggregate() {
    /* WORKING IN BOTH HAPPY AND EXCEPTION PATHS */

    return IntegrationFlows.from(this.echoChannel())
        .log()
        .aggregate(aggregator -> aggregator
            .messageStore(new SimpleMessageStore()) // Infinite capacity
            .correlationStrategy(new HeaderAttributeCorrelationStrategy("correlationId"))
            .releaseStrategy(new MessageCountReleaseStrategy(5)) // Max batch size
            .groupTimeout(1000)  // Timeout for releasing the batch
            .expireGroupsUponCompletion(true)
            .expireGroupsUponTimeout(true)
            .sendPartialResultOnExpiry(true)
            .outputProcessor(group -> {
              log.info("IntegrationConfig.processEcho message count: {}", CollectionUtils.size(group.getMessages()));

              try {
                List<Integer> inputs = StreamEx.of(group.getMessages())
                    .map(message -> (Integer) message.getPayload())
                    .toList();

                this.mathService.echo(inputs);

                List<Pair<Object, Object>> payload = StreamEx.of(group.getMessages())
                    .map(message ->
                             Pair.of(
                                 message.getHeaders().getReplyChannel(),
                                 null
                             )
                    ).toList();

                return MessageBuilder.withPayload(payload).build();

              } catch (Exception ex) {
                log.info("IntegrationConfig.processMultiplication grouping exception: {}", CollectionUtils.size(group.getMessages()));

                List<Pair<Object, Exception>> payload = StreamEx.of(group.getMessages())
                    .map(message -> Pair.of(message.getHeaders().getReplyChannel(), ex))
                    .toList();

                return MessageBuilder.withPayload(payload).build();
              }
            })
        )
        .log()
        .split()
        .log()
        .handle(Pair.class, (payload, headers) -> {
          Object result = (payload.getRight() instanceof Throwable) ? payload.getRight() : Optional.ofNullable(payload.getRight());

          return MessageBuilder.withPayload(result)
              .copyHeaders(headers)
              .setReplyChannel((MessageChannel) payload.getLeft())
              .setErrorChannel((MessageChannel) payload.getLeft())
              .build();
        })
        .log()
        .handle((payload, headers) -> payload)
        .get();
  }
}
