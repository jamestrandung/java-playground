package com.james.playground.temporal.configs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties.Value;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.MarketingWorkflow;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.spring.boot.WorkerOptionsCustomizer;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.WorkflowImplementationOptions;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TemporalConfigs {
  @Bean
  public WorkerOptionsCustomizer workerOptions() {
    return new WorkerOptionsCustomizer() {
      @Nonnull
      @Override
      public WorkerOptions.Builder customize(
          @Nonnull WorkerOptions.Builder optionsBuilder,
          @Nonnull String workerName,
          @Nonnull String taskQueue
      ) {
        if (taskQueue.equals(MarketingWorkflow.QUEUE_NAME)) {
          optionsBuilder.setLocalActivityWorkerOnly(true);
        }

        return optionsBuilder;
      }
    };
  }

  @Bean
  public TemporalOptionsCustomizer<WorkflowServiceStubsOptions.Builder> workflowServiceStubsOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkflowServiceStubsOptions.Builder customize(@Nonnull WorkflowServiceStubsOptions.Builder optionsBuilder) {
        return optionsBuilder;
      }
    };
  }

  @Bean
  public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> workflowClientOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkflowClientOptions.Builder customize(@Nonnull WorkflowClientOptions.Builder optionsBuilder) {
        Builder jsonMapperBuilder = JsonMapper.builder()
            .addModule(new JavaTimeModule());

        for (Class<? extends WorkflowNode> clazz : WorkflowNode.EXISTING_IMPLEMENTATIONS) {
          jsonMapperBuilder = jsonMapperBuilder.withConfigOverride(clazz, configs -> {
            configs.setIgnorals(
                Value.forIgnoredProperties(WorkflowNode.IGNORABLE_FIELDS_FOR_WORKFLOW_EXECUTION)
                    .withIgnoreUnknown()
            );
          });
        }

        JacksonJsonPayloadConverter payloadConverter = new JacksonJsonPayloadConverter(jsonMapperBuilder.build());

        DefaultDataConverter dataConverter = DefaultDataConverter.newDefaultInstance()
            .withPayloadConverterOverrides(payloadConverter);

        return optionsBuilder.setDataConverter(dataConverter);
      }
    };
  }

  @Bean
  public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder> workerFactoryOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkerFactoryOptions.Builder customize(@Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
        return optionsBuilder;
      }
    };
  }

  @Bean
  public TemporalOptionsCustomizer<WorkflowImplementationOptions.Builder> workflowImplementationOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public WorkflowImplementationOptions.Builder customize(@Nonnull WorkflowImplementationOptions.Builder optionsBuilder) {
        return optionsBuilder;
      }
    };
  }

  @Bean
  public TemporalOptionsCustomizer<TestEnvironmentOptions.Builder> testEnvironmentOptions() {
    return new TemporalOptionsCustomizer<>() {
      @Nonnull
      @Override
      public TestEnvironmentOptions.Builder customize(@Nonnull TestEnvironmentOptions.Builder optionsBuilder) {
        return optionsBuilder;
      }
    };
  }
}
