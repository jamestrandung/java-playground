package com.james.playground.batch;

import java.util.List;
import java.util.Map;
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
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

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
    return new DirectChannel();
  }

  @Bean
  public MessageChannel sumChannel() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow processMultiplicationSingle() {
    /* WORKING IN BOTH HAPPY AND EXCEPTION PATHS */

    return IntegrationFlows.from(this.singleChannel())
        .handle((payload, headers) -> mathService.multiplyByTwoExceptionally((Integer) payload))
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

    return IntegrationFlows.from(this.aggregateChannel())
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
              log.info("IntegrationConfig.processMultiplication message count: {}", CollectionUtils.size(group.getMessages()));

              try {
                List<Integer> inputs = StreamEx.of(group.getMessages())
                    .map(message -> (Integer) message.getPayload())
                    .toList();

                Map<Integer, Integer> results = mathService.multiplyListByTwo(inputs);

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
        .split()
        .log()
        .handle(Pair.class, (payload, headers) ->
            MessageBuilder.withPayload(payload.getRight())
                .copyHeaders(headers)
                .setReplyChannel((MessageChannel) payload.getLeft())
                .setErrorChannel((MessageChannel) payload.getLeft())
                .build()
        )
        .log()
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

    return IntegrationFlows.from(this.sumChannel())
        .log()
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
                int sum = StreamEx.of(group.getMessages())
                    .map(message -> (Integer) message.getPayload())
                    .toListAndThen(mathService::sum);

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
        )
        .log()
        .split()
        .log()
        .handle(Pair.class, (payload, headers) ->
            MessageBuilder.withPayload(payload.getRight())
                .copyHeaders(headers)
                .setReplyChannel((MessageChannel) payload.getLeft())
                .setErrorChannel((MessageChannel) payload.getLeft())
                .build()
        )
        .log()
        .handle((payload, headers) -> payload)
        .get();

    //    return IntegrationFlows.from(this.sumChannel())
    //        .log()
    //        .aggregate(aggregator -> aggregator
    //            .messageStore(new SimpleMessageStore()) // Infinite capacity
    //            .correlationStrategy(new HeaderAttributeCorrelationStrategy("correlationId"))
    //            .releaseStrategy(new MessageCountReleaseStrategy(5)) // Max batch size
    //            .groupTimeout(1000)  // Timeout for releasing the batch
    //            .expireGroupsUponCompletion(true)
    //            .expireGroupsUponTimeout(true)
    //            .sendPartialResultOnExpiry(true)
    //            //            .async(true)
    //            .outputProcessor(group -> {
    //              log.info("IntegrationConfig.processSumAggregate message count: {}", CollectionUtils.size(group.getMessages()));
    //
    //              try {
    //                return StreamEx.of(group.getMessages())
    //                    .map(message -> (Integer) message.getPayload())
    //                    .toListAndThen(mathService::sum);
    //
    //              } catch (Exception ex) {
    //                log.info("IntegrationConfig.processSumAggregate grouping exception: {}", CollectionUtils.size(group.getMessages()));
    //
    //                return ex;
    //              }
    //            })
    //        )
    //        .log()
    //        .handle((payload, headers) -> payload)
    //        .get();
  }
}
